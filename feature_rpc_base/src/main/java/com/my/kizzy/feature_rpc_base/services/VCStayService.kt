/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * VCStayService.kt is part of Kizzy
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for maintaining a 24/7 voice channel connection.
 * This allows users to stay connected to a voice channel continuously
 * without needing to keep the app in the foreground.
 */
@AndroidEntryPoint
class VCStayService : Service() {
    private var guildId: String? = null
    private var channelId: String? = null
    private var selfMute: Boolean = true
    private var selfDeaf: Boolean = true
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
            selfMute = intent?.getBooleanExtra(EXTRA_SELF_MUTE, true) ?: true
            selfDeaf = intent?.getBooleanExtra(EXTRA_SELF_DEAF, true) ?: true

            if (guildId == null || channelId == null) {
                logger.e("VCStayService", "Guild ID or Channel ID is null, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            // Save last used VC config
            Prefs[Prefs.LAST_VC_GUILD_ID] = guildId!!
            Prefs[Prefs.LAST_VC_CHANNEL_ID] = channelId!!
            Prefs[Prefs.LAST_VC_SELF_MUTE] = selfMute
            Prefs[Prefs.LAST_VC_SELF_DEAF] = selfDeaf

            val stopIntent = Intent(this, VCStayService::class.java)
            stopIntent.action = Constants.ACTION_STOP_SERVICE
            val pendingIntent: PendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
            )

            startForeground(
                VC_STAY_NOTIFICATION_ID, notificationBuilder
                    .setContentTitle(getString(R.string.vc_stay_running))
                    .setContentText(getString(R.string.vc_stay_connected))
                    .setSmallIcon(R.drawable.ic_vc_stay)
                    .addAction(R.drawable.ic_vc_stay, getString(R.string.exit), pendingIntent)
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
                    
                    discordWebSocket.joinVoiceChannel(
                        guildId = guildId!!,
                        channelId = channelId!!,
                        selfMute = selfMute,
                        selfDeaf = selfDeaf
                    )
                    logger.i("VCStayService", "Joined voice channel: $channelId in guild: $guildId")
                    
                    // Update notification to show connected state
                    notificationManager.notify(
                        VC_STAY_NOTIFICATION_ID,
                        notificationBuilder
                            .setContentTitle(getString(R.string.vc_stay_running))
                            .setContentText(getString(R.string.vc_stay_connected))
                            .build()
                    )
                } catch (e: Exception) {
                    logger.e("VCStayService", "Error joining voice channel: ${e.message}")
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.launch {
            try {
                guildId?.let {
                    discordWebSocket.leaveVoiceChannel(it)
                    logger.i("VCStayService", "Left voice channel in guild: $it")
                }
            } catch (e: Exception) {
                logger.e("VCStayService", "Error leaving voice channel: ${e.message}")
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
        const val WAKELOCK = "kizzy:VCStay"
        const val VC_STAY_NOTIFICATION_ID = 2022_03_05
        const val EXTRA_GUILD_ID = "guild_id"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_SELF_MUTE = "self_mute"
        const val EXTRA_SELF_DEAF = "self_deaf"
    }
}
