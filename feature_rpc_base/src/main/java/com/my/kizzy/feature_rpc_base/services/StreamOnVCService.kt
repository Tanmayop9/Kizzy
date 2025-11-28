/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * StreamOnVCService.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for streaming in a Discord voice channel.
 * This allows users to go live in a voice channel with a YouTube URL,
 * creating a streaming presence visible to other Discord users.
 */
@AndroidEntryPoint
class StreamOnVCService : Service() {
    private var guildId: String? = null
    private var channelId: String? = null
    private var youtubeUrl: String? = null
    private var streamName: String? = null
    private var wakeLock: WakeLock? = null

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
            youtubeUrl = intent?.getStringExtra(EXTRA_YOUTUBE_URL)
            streamName = intent?.getStringExtra(EXTRA_STREAM_NAME)

            if (guildId == null || channelId == null || youtubeUrl == null) {
                logger.e("StreamOnVCService", "Guild ID, Channel ID, or YouTube URL is null, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            // Validate YouTube URL
            if (!isValidYoutubeUrl(youtubeUrl!!)) {
                logger.e("StreamOnVCService", "Invalid YouTube URL, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            // Save last used stream config
            Prefs[Prefs.STREAM_VC_GUILD_ID] = guildId!!
            Prefs[Prefs.STREAM_VC_CHANNEL_ID] = channelId!!
            Prefs[Prefs.STREAM_VC_YOUTUBE_URL] = youtubeUrl!!
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
                    delay(2000)
                    
                    // Join the voice channel first
                    discordWebSocket.joinVoiceChannel(
                        guildId = guildId!!,
                        channelId = channelId!!,
                        selfMute = false,
                        selfDeaf = false
                    )
                    logger.i("StreamOnVCService", "Joined voice channel: $channelId in guild: $guildId")
                    
                    // Small delay before setting streaming presence
                    delay(1000)
                    
                    // Set streaming presence with YouTube URL
                    val streamingPresence = Presence(
                        activities = listOf(
                            Activity(
                                name = streamName ?: "YouTube",
                                type = 1, // Streaming type
                                url = youtubeUrl,
                                timestamps = Timestamps(start = System.currentTimeMillis())
                            )
                        ),
                        afk = false,
                        since = System.currentTimeMillis(),
                        status = "online"
                    )
                    discordWebSocket.sendActivity(streamingPresence)
                    logger.i("StreamOnVCService", "Started streaming: $streamName with URL: $youtubeUrl")
                    
                    // Update notification to show streaming state
                    notificationManager.notify(
                        STREAM_VC_NOTIFICATION_ID,
                        notificationBuilder
                            .setContentTitle(getString(R.string.stream_vc_running))
                            .setContentText(streamName ?: getString(R.string.stream_vc_streaming))
                            .build()
                    )
                } catch (e: Exception) {
                    logger.e("StreamOnVCService", "Error starting stream: ${e.message}")
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun isValidYoutubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    override fun onDestroy() {
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
    }
}
