/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * VCStayScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of Tanmay
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_vc_stay

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(id = R.string.vc_stay_how_to_get_ids)) 
                }
            },
            text = { Text(stringResource(id = R.string.vc_stay_ids_instructions)) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            shape = RoundedCornerShape(16.dp)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
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
            }

            item {
                AnimatedVisibility(
                    visible = vcStayRunning,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ConnectionStatusCard()
                }
            }

            item {
                // Server Configuration Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Server Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        VCStyledTextField(
                            value = guildId,
                            onValueChange = { 
                                guildId = it
                                Prefs[Prefs.LAST_VC_GUILD_ID] = it
                            },
                            label = stringResource(id = R.string.vc_stay_guild_id),
                            icon = Icons.Default.Forum,
                            enabled = !vcStayRunning
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        VCStyledTextField(
                            value = channelId,
                            onValueChange = { 
                                channelId = it
                                Prefs[Prefs.LAST_VC_CHANNEL_ID] = it
                            },
                            label = stringResource(id = R.string.vc_stay_channel_id),
                            icon = Icons.Default.Headset,
                            enabled = !vcStayRunning
                        )
                    }
                }
            }

            item {
                // Voice Settings Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Voice Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
                        )
                        
                        PreferenceSwitch(
                            title = stringResource(id = R.string.vc_stay_self_mute),
                            icon = Icons.Default.MicOff,
                            isChecked = selfMute,
                            enabled = !vcStayRunning
                        ) {
                            selfMute = !selfMute
                            Prefs[Prefs.LAST_VC_SELF_MUTE] = selfMute
                        }

                        PreferenceSwitch(
                            title = stringResource(id = R.string.vc_stay_self_deaf),
                            icon = Icons.Default.HeadsetOff,
                            isChecked = selfDeaf,
                            enabled = !vcStayRunning
                        ) {
                            selfDeaf = !selfDeaf
                            Prefs[Prefs.LAST_VC_SELF_DEAF] = selfDeaf
                        }

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
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun VCStyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun ConnectionStatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VoiceChat,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column {
                Text(
                    text = stringResource(id = R.string.connection_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(id = R.string.connected),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
