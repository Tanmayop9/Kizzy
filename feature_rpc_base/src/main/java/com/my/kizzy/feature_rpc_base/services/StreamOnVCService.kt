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
import kizzy.gateway.entities.presence.Presence
import kizzy.gateway.entities.presence.Timestamps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for streaming in a Discord voice channel.
 * This allows users to go live in a voice channel with a YouTube or Twitch URL,
 * creating a streaming presence visible to other Discord users.
 * 
 * The streaming works by:
 * 1. Connecting to Discord Gateway
 * 2. Joining the specified voice channel
 * 3. Setting a streaming activity (type 1) with a valid YouTube/Twitch URL
 * 
 * Discord will show the user as "Streaming" with a purple indicator.
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

            // Validate URL
            if (!isValidStreamUrl(streamUrl!!)) {
                logger.e("StreamOnVCService", "Invalid stream URL, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

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

            startForeground(
                STREAM_VC_NOTIFICATION_ID, notificationBuilder
                    .setContentTitle(getString(R.string.stream_vc_running))
                    .setContentText(getString(R.string.stream_vc_streaming))
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
                        name = streamName ?: getDefaultStreamName(streamUrl!!),
                        url = streamUrl!!
                    )
                    discordWebSocket.sendActivity(streamingPresence)
                    logger.i("StreamOnVCService", "Started streaming: ${streamName ?: getDefaultStreamName(streamUrl!!)} with URL: $streamUrl")
                    
                    // Update notification to show streaming state
                    notificationManager.notify(
                        STREAM_VC_NOTIFICATION_ID,
                        notificationBuilder
                            .setContentTitle(getString(R.string.stream_vc_running))
                            .setContentText(streamName ?: getString(R.string.stream_vc_streaming))
                            .build()
                    )
                    
                    // Start keep-alive job to maintain the streaming presence
                    startKeepAliveJob()
                    
                } catch (e: Exception) {
                    logger.e("StreamOnVCService", "Error starting stream: ${e.message}")
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }
    
    /**
     * Creates a streaming presence activity.
     * Type 1 activity with a valid Twitch or YouTube URL will show as "Streaming" in Discord.
     */
    private fun createStreamingPresence(name: String, url: String): Presence {
        return Presence(
            activities = listOf(
                Activity(
                    name = name,
                    type = 1, // Streaming type - shows as "Streaming" with purple indicator
                    state = "Live on ${getPlatformName(url)}",
                    details = "Streaming via NeroxStatus",
                    url = url,
                    timestamps = Timestamps(start = System.currentTimeMillis())
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
    private fun startKeepAliveJob() {
        keepAliveJob?.cancel()
        keepAliveJob = scope.launch {
            while (isActive) {
                delay(KEEP_ALIVE_INTERVAL_MS)
                try {
                    if (discordWebSocket.isWebSocketConnected()) {
                        // Refresh the streaming presence to keep it active
                        val streamingPresence = createStreamingPresence(
                            name = streamName ?: getDefaultStreamName(streamUrl!!),
                            url = streamUrl!!
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
    
    /**
     * Gets the platform name from the URL for display purposes.
     */
    private fun getPlatformName(url: String): String {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
            url.contains("twitch.tv") -> "Twitch"
            else -> "Stream"
        }
    }
    
    /**
     * Gets a default stream name based on the URL platform.
     */
    private fun getDefaultStreamName(url: String): String {
        return getPlatformName(url)
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
    }
}
