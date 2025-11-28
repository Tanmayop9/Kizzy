/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * StreamOnVCService.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of Tanmay
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_rpc_base.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.my.kizzy.domain.interfaces.Logger
import com.my.kizzy.feature_rpc_base.Constants
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import dagger.hilt.android.AndroidEntryPoint
import kizzy.gateway.DiscordWebSocket
import kizzy.gateway.entities.presence.Activity
import kizzy.gateway.entities.presence.Assets
import kizzy.gateway.entities.presence.Presence
import kizzy.gateway.entities.presence.Timestamps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for streaming video content in a Discord voice channel.
 * This allows users to go live in a voice channel with a YouTube or Twitch URL,
 * creating a streaming presence visible to other Discord users.
 * 
 * Enhanced in v7.0 with:
 * - Video URL conversion and extraction
 * - Better platform detection (YouTube, Twitch)
 * - Video thumbnail as large image
 * - Video title extraction from URL
 * 
 * The streaming works by:
 * 1. Connecting to Discord Gateway
 * 2. Joining the specified voice channel
 * 3. Setting a streaming activity (type 1) with a valid YouTube/Twitch URL
 * 4. Discord shows the user as "Streaming" with a purple LIVE indicator
 * 
 * Note: This creates a streaming presence. Actual video playback in VC
 * would require WebRTC and UDP voice server connections which are
 * complex to implement on mobile.
 */
@AndroidEntryPoint
class StreamOnVCService : Service() {
    private var guildId: String? = null
    private var channelId: String? = null
    private var streamUrl: String? = null
    private var streamName: String? = null
    private var wakeLock: WakeLock? = null
    private var keepAliveJob: kotlinx.coroutines.Job? = null

    @Inject
    lateinit var discordWebSocket: DiscordWebSocket

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var notificationBuilder: Notification.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var logger: Logger

    @SuppressLint("WakelockTimeout")
    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(Constants.ACTION_STOP_SERVICE)) {
            stopSelf()
        } else {
            guildId = intent?.getStringExtra(EXTRA_GUILD_ID)
            channelId = intent?.getStringExtra(EXTRA_CHANNEL_ID)
            streamUrl = intent?.getStringExtra(EXTRA_YOUTUBE_URL)
            streamName = intent?.getStringExtra(EXTRA_STREAM_NAME)

            if (guildId == null || channelId == null || streamUrl == null) {
                logger.e("StreamOnVCService", "Guild ID, Channel ID, or Stream URL is null, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            // Validate and convert URL
            val validatedUrl = convertToValidStreamUrl(streamUrl!!)
            if (validatedUrl == null) {
                logger.e("StreamOnVCService", "Invalid stream URL, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
            streamUrl = validatedUrl

            // Save last used stream config
            Prefs[Prefs.STREAM_VC_GUILD_ID] = guildId!!
            Prefs[Prefs.STREAM_VC_CHANNEL_ID] = channelId!!
            Prefs[Prefs.STREAM_VC_YOUTUBE_URL] = streamUrl!!
            Prefs[Prefs.STREAM_VC_STREAM_NAME] = streamName ?: ""

            val stopIntent = Intent(this, StreamOnVCService::class.java)
            stopIntent.action = Constants.ACTION_STOP_SERVICE
            val pendingIntent: PendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
            )

            // Get video info for notification
            val videoInfo = extractVideoInfo(streamUrl!!)
            val notificationTitle = getString(R.string.stream_vc_running)
            val notificationText = if (streamName.isNullOrEmpty()) {
                "${getString(R.string.stream_vc_streaming)} - ${videoInfo.platform}"
            } else {
                "$streamName - ${videoInfo.platform}"
            }

            startForeground(
                STREAM_VC_NOTIFICATION_ID, notificationBuilder
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setSmallIcon(R.drawable.ic_stream_vc)
                    .addAction(R.drawable.ic_stream_vc, getString(R.string.exit), pendingIntent)
                    .build()
            )

            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK)
            wakeLock?.acquire()

            scope.launch {
                try {
                    discordWebSocket.connect()
                    // Small delay to ensure connection is established
                    delay(CONNECTION_DELAY_MS)
                    
                    // Join the voice channel first
                    discordWebSocket.joinVoiceChannel(
                        guildId = guildId!!,
                        channelId = channelId!!,
                        selfMute = false,
                        selfDeaf = false
                    )
                    logger.i("StreamOnVCService", "Joined voice channel: $channelId in guild: $guildId")
                    
                    // Small delay before setting streaming presence
                    delay(PRESENCE_DELAY_MS)
                    
                    // Set streaming presence with YouTube/Twitch URL
                    // Type 1 = Streaming - Discord will show purple "LIVE" indicator
                    val streamingPresence = createStreamingPresence(
                        name = streamName ?: videoInfo.defaultName,
                        url = streamUrl!!,
                        videoInfo = videoInfo
                    )
                    discordWebSocket.sendActivity(streamingPresence)
                    logger.i("StreamOnVCService", "Started streaming: ${streamName ?: videoInfo.defaultName} with URL: $streamUrl")
                    logger.i("StreamOnVCService", "Video ID: ${videoInfo.videoId}, Platform: ${videoInfo.platform}")
                    
                    // Update notification to show streaming state with video info
                    notificationManager.notify(
                        STREAM_VC_NOTIFICATION_ID,
                        notificationBuilder
                            .setContentTitle(getString(R.string.stream_vc_running))
                            .setContentText("${streamName ?: videoInfo.defaultName} - LIVE on ${videoInfo.platform}")
                            .build()
                    )
                    
                    // Start keep-alive job to maintain the streaming presence
                    startKeepAliveJob(videoInfo)
                    
                } catch (e: Exception) {
                    logger.e("StreamOnVCService", "Error starting stream: ${e.message}")
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }
    
    /**
     * Creates a streaming presence activity with video information.
     * Type 1 activity with a valid Twitch or YouTube URL will show as "Streaming" in Discord.
     */
    private fun createStreamingPresence(name: String, url: String, videoInfo: VideoInfo): Presence {
        return Presence(
            activities = listOf(
                Activity(
                    name = name,
                    type = 1, // Streaming type - shows as "Streaming" with purple indicator
                    state = "Live on ${videoInfo.platform}",
                    details = if (videoInfo.videoId != null) "Video: ${videoInfo.videoId}" else "Streaming via NeroxStatus v7",
                    url = url,
                    timestamps = Timestamps(start = System.currentTimeMillis()),
                    assets = if (videoInfo.thumbnailUrl != null) {
                        Assets(
                            largeImage = videoInfo.thumbnailUrl,
                            largeText = "Streaming ${videoInfo.platform}",
                            smallImage = null,
                            smallText = null
                        )
                    } else null
                )
            ),
            afk = false,
            since = System.currentTimeMillis(),
            status = "online"
        )
    }
    
    /**
     * Starts a keep-alive job that periodically refreshes the streaming presence
     * to ensure it stays active.
     */
    private fun startKeepAliveJob(videoInfo: VideoInfo) {
        keepAliveJob?.cancel()
        keepAliveJob = scope.launch {
            while (isActive) {
                delay(KEEP_ALIVE_INTERVAL_MS)
                try {
                    if (discordWebSocket.isWebSocketConnected()) {
                        // Refresh the streaming presence to keep it active
                        val streamingPresence = createStreamingPresence(
                            name = streamName ?: videoInfo.defaultName,
                            url = streamUrl!!,
                            videoInfo = videoInfo
                        )
                        discordWebSocket.sendActivity(streamingPresence)
                        logger.d("StreamOnVCService", "Keep-alive: Refreshed streaming presence")
                    } else {
                        logger.w("StreamOnVCService", "Keep-alive: WebSocket not connected, attempting reconnect")
                        // WebSocket might have disconnected, let the reconnect logic handle it
                    }
                } catch (e: Exception) {
                    logger.e("StreamOnVCService", "Keep-alive error: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        keepAliveJob?.cancel()
        scope.launch {
            try {
                guildId?.let {
                    discordWebSocket.leaveVoiceChannel(it)
                    logger.i("StreamOnVCService", "Left voice channel in guild: $it")
                }
            } catch (e: Exception) {
                logger.e("StreamOnVCService", "Error leaving voice channel: ${e.message}")
            }
        }
        scope.cancel()
        discordWebSocket.close()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Data class to hold extracted video information
     */
    data class VideoInfo(
        val platform: String,
        val videoId: String?,
        val thumbnailUrl: String?,
        val defaultName: String
    )

    companion object {
        const val WAKELOCK = "kizzy:StreamOnVC"
        const val STREAM_VC_NOTIFICATION_ID = 2022_03_06
        const val EXTRA_GUILD_ID = "guild_id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_YOUTUBE_URL = "youtube_url"
        const val EXTRA_STREAM_NAME = "stream_name"
        
        // Delay constants for connection establishment
        private const val CONNECTION_DELAY_MS = 2000L
        private const val PRESENCE_DELAY_MS = 1000L
        
        // Keep-alive interval to refresh streaming presence (5 minutes)
        private const val KEEP_ALIVE_INTERVAL_MS = 300_000L
        
        // Stream URL patterns for validation
        // Discord only shows "LIVE" streaming for Twitch and YouTube URLs
        private val STREAM_URL_PATTERNS = listOf(
            "youtube.com/watch",
            "youtube.com/live",
            "youtu.be/",
            "youtube.com/shorts",
            "twitch.tv/"
        )
        
        // Regex patterns for extracting video IDs
        private val YOUTUBE_VIDEO_ID_PATTERNS = listOf(
            Regex("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/live/([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/shorts/([a-zA-Z0-9_-]{11})")
        )
        private val TWITCH_CHANNEL_PATTERN = Regex("twitch\\.tv/([a-zA-Z0-9_]+)")
        
        /**
         * Validates if the given URL is a valid YouTube URL.
         * This can be used by both the service and the UI screen.
         * @deprecated Use [isValidStreamUrl] instead
         */
        fun isValidYoutubeUrl(url: String): Boolean {
            return isValidStreamUrl(url)
        }
        
        /**
         * Validates if the given URL is a valid streaming URL (YouTube or Twitch).
         * Discord only supports Twitch and YouTube URLs for streaming presence.
         */
        fun isValidStreamUrl(url: String): Boolean {
            if (url.isBlank()) return false
            val lowercaseUrl = url.lowercase()
            return STREAM_URL_PATTERNS.any { pattern -> lowercaseUrl.contains(pattern) }
        }
        
        /**
         * Converts various video URL formats to a valid streaming URL.
         * Handles short URLs, mobile URLs, and extracts video IDs.
         * 
         * @param url The input URL to convert
         * @return A valid streaming URL, or null if the URL is invalid
         */
        fun convertToValidStreamUrl(url: String): String? {
            if (url.isBlank()) return null
            
            var cleanUrl = url.trim()
            
            // Add https:// if missing
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                cleanUrl = "https://$cleanUrl"
            }
            
            // Convert http to https
            cleanUrl = cleanUrl.replace("http://", "https://")
            
            // Handle YouTube URLs
            if (cleanUrl.contains("youtube.com") || cleanUrl.contains("youtu.be")) {
                // Extract video ID and convert to standard format
                val videoId = extractYoutubeVideoId(cleanUrl)
                if (videoId != null) {
                    return "https://www.youtube.com/watch?v=$videoId"
                }
            }
            
            // Handle Twitch URLs
            if (cleanUrl.contains("twitch.tv")) {
                val channelMatch = TWITCH_CHANNEL_PATTERN.find(cleanUrl)
                if (channelMatch != null) {
                    val channel = channelMatch.groupValues[1]
                    return "https://www.twitch.tv/$channel"
                }
            }
            
            // If URL passes validation, return it as-is
            return if (isValidStreamUrl(cleanUrl)) cleanUrl else null
        }
        
        /**
         * Extracts the YouTube video ID from various URL formats.
         */
        fun extractYoutubeVideoId(url: String): String? {
            for (pattern in YOUTUBE_VIDEO_ID_PATTERNS) {
                val match = pattern.find(url)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
            return null
        }
        
        /**
         * Extracts video information from a streaming URL.
         */
        fun extractVideoInfo(url: String): VideoInfo {
            val lowercaseUrl = url.lowercase()
            
            return when {
                lowercaseUrl.contains("youtube.com") || lowercaseUrl.contains("youtu.be") -> {
                    val videoId = extractYoutubeVideoId(url)
                    VideoInfo(
                        platform = "YouTube",
                        videoId = videoId,
                        thumbnailUrl = videoId?.let { "https://img.youtube.com/vi/$it/maxresdefault.jpg" },
                        defaultName = "YouTube Video"
                    )
                }
                lowercaseUrl.contains("twitch.tv") -> {
                    val channelMatch = TWITCH_CHANNEL_PATTERN.find(url)
                    val channel = channelMatch?.groupValues?.get(1)
                    VideoInfo(
                        platform = "Twitch",
                        videoId = channel,
                        thumbnailUrl = null, // Twitch requires API call for thumbnails
                        defaultName = channel?.let { "Watching $it" } ?: "Twitch Stream"
                    )
                }
                else -> VideoInfo(
                    platform = "Stream",
                    videoId = null,
                    thumbnailUrl = null,
                    defaultName = "Live Stream"
                )
            }
        }
    }
}
