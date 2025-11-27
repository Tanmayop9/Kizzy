/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * VCStayScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_vc_stay

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.feature_rpc_base.services.AppDetectionService
import com.my.kizzy.feature_rpc_base.services.CustomRpcService
import com.my.kizzy.feature_rpc_base.services.ExperimentalRpc
import com.my.kizzy.feature_rpc_base.services.MediaRpcService
import com.my.kizzy.feature_rpc_base.services.VCStayService
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.SwitchBar
import com.my.kizzy.ui.components.preference.PreferenceSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VCStayScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var vcStayRunning by remember { mutableStateOf(AppUtils.vcStayRunning()) }
    var guildId by remember { mutableStateOf(Prefs[Prefs.LAST_VC_GUILD_ID, ""]) }
    var channelId by remember { mutableStateOf(Prefs[Prefs.LAST_VC_CHANNEL_ID, ""]) }
    var selfMute by remember { mutableStateOf(Prefs[Prefs.LAST_VC_SELF_MUTE, true]) }
    var selfDeaf by remember { mutableStateOf(Prefs[Prefs.LAST_VC_SELF_DEAF, true]) }
    var autoReconnect by remember { mutableStateOf(Prefs[Prefs.VC_STAY_AUTO_RECONNECT, true]) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(id = R.string.vc_stay_how_to_get_ids)) },
            text = { Text(stringResource(id = R.string.vc_stay_ids_instructions)) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.main_vcStay),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = { BackButton { onBackPressed() } },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Help"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SwitchBar(
                title = stringResource(id = R.string.enable_vc_stay),
                isChecked = vcStayRunning,
                enabled = guildId.isNotEmpty() && channelId.isNotEmpty()
            ) {
                vcStayRunning = !vcStayRunning
                when (vcStayRunning) {
                    true -> {
                        // Stop other services first
                        context.stopService(Intent(context, AppDetectionService::class.java))
                        context.stopService(Intent(context, CustomRpcService::class.java))
                        context.stopService(Intent(context, ExperimentalRpc::class.java))
                        context.stopService(Intent(context, MediaRpcService::class.java))
                        
                        // Start VC Stay service
                        val intent = Intent(context, VCStayService::class.java).apply {
                            putExtra(VCStayService.EXTRA_GUILD_ID, guildId)
                            putExtra(VCStayService.EXTRA_CHANNEL_ID, channelId)
                            putExtra(VCStayService.EXTRA_SELF_MUTE, selfMute)
                            putExtra(VCStayService.EXTRA_SELF_DEAF, selfDeaf)
                        }
                        context.startService(intent)
                    }
                    false -> {
                        context.stopService(Intent(context, VCStayService::class.java))
                    }
                }
            }

            LazyColumn {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = guildId,
                        onValueChange = { 
                            guildId = it
                            Prefs[Prefs.LAST_VC_GUILD_ID] = it
                        },
                        label = { Text(stringResource(id = R.string.vc_stay_guild_id)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = channelId,
                        onValueChange = { 
                            channelId = it
                            Prefs[Prefs.LAST_VC_CHANNEL_ID] = it
                        },
                        label = { Text(stringResource(id = R.string.vc_stay_channel_id)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PreferenceSwitch(
                        title = stringResource(id = R.string.vc_stay_self_mute),
                        icon = Icons.Default.MicOff,
                        isChecked = selfMute,
                    ) {
                        selfMute = !selfMute
                        Prefs[Prefs.LAST_VC_SELF_MUTE] = selfMute
                    }
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.vc_stay_self_deaf),
                        icon = Icons.Default.HeadsetOff,
                        isChecked = selfDeaf,
                    ) {
                        selfDeaf = !selfDeaf
                        Prefs[Prefs.LAST_VC_SELF_DEAF] = selfDeaf
                    }
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.vc_stay_auto_reconnect),
                        description = stringResource(id = R.string.vc_stay_auto_reconnect_desc),
                        icon = Icons.Default.Refresh,
                        isChecked = autoReconnect,
                    ) {
                        autoReconnect = !autoReconnect
                        Prefs[Prefs.VC_STAY_AUTO_RECONNECT] = autoReconnect
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = vcStayRunning) {
                        ConnectionStatusCard()
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard() {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.connection_status),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.connected),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
