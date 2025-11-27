/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * BootReceiver.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_rpc_base.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.my.kizzy.preference.Prefs

/**
 * Broadcast receiver that starts RPC service on device boot.
 * This enables the auto-start functionality when the device boots up.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val autoStartEnabled = Prefs[Prefs.AUTO_START_RPC_ON_BOOT, false]
            
            if (autoStartEnabled) {
                val rpcType = Prefs[Prefs.AUTO_START_RPC_TYPE, "custom"]
                
                when (rpcType) {
                    "app_detection" -> {
                        context.startService(Intent(context, AppDetectionService::class.java))
                    }
                    "media" -> {
                        context.startService(Intent(context, MediaRpcService::class.java))
                    }
                    "custom" -> {
                        val lastRpc = Prefs[Prefs.LAST_RUN_CUSTOM_RPC, ""]
                        if (lastRpc.isNotEmpty()) {
                            val serviceIntent = Intent(context, CustomRpcService::class.java).apply {
                                putExtra("RPC", lastRpc)
                            }
                            context.startService(serviceIntent)
                        }
                    }
                    "experimental" -> {
                        context.startService(Intent(context, ExperimentalRpc::class.java))
                    }
                    "vc_stay" -> {
                        val guildId = Prefs[Prefs.LAST_VC_GUILD_ID, ""]
                        val channelId = Prefs[Prefs.LAST_VC_CHANNEL_ID, ""]
                        if (guildId.isNotEmpty() && channelId.isNotEmpty()) {
                            val serviceIntent = Intent(context, VCStayService::class.java).apply {
                                putExtra(VCStayService.EXTRA_GUILD_ID, guildId)
                                putExtra(VCStayService.EXTRA_CHANNEL_ID, channelId)
                                putExtra(VCStayService.EXTRA_SELF_MUTE, Prefs[Prefs.LAST_VC_SELF_MUTE, true])
                                putExtra(VCStayService.EXTRA_SELF_DEAF, Prefs[Prefs.LAST_VC_SELF_DEAF, true])
                            }
                            context.startService(serviceIntent)
                        }
                    }
                }
            }
        }
    }
}
