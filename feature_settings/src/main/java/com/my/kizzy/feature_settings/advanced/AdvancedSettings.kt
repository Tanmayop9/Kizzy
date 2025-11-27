/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AdvancedSettings.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_settings.advanced

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.SettingItem
import com.my.kizzy.ui.components.Subtitle
import com.my.kizzy.ui.components.dialog.SingleChoiceItem
import com.my.kizzy.ui.components.preference.PreferenceSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettings(onBackPressed: () -> Boolean) {
    val context = LocalContext.current
    
    // Auto-start preferences
    var autoStartEnabled by remember { mutableStateOf(Prefs[Prefs.AUTO_START_RPC_ON_BOOT, false]) }
    var autoStartType by remember { mutableStateOf(Prefs[Prefs.AUTO_START_RPC_TYPE, "custom"]) }
    var showAutoStartTypeDialog by remember { mutableStateOf(false) }
    
    // Battery saver
    var batterySaverEnabled by remember { mutableStateOf(Prefs[Prefs.BATTERY_SAVER_MODE, false]) }
    
    // Connection status
    var showConnectionStatus by remember { mutableStateOf(Prefs[Prefs.SHOW_CONNECTION_STATUS, true]) }
    
    // Invisible mode
    var invisibleMode by remember { mutableStateOf(Prefs[Prefs.INVISIBLE_MODE, false]) }
    
    // Random RPC rotation
    var randomRotation by remember { mutableStateOf(Prefs[Prefs.RANDOM_RPC_ROTATION, false]) }
    var rotationInterval by remember { mutableStateOf(Prefs[Prefs.RANDOM_RPC_INTERVAL, 30]) }
    var showRotationIntervalDialog by remember { mutableStateOf(false) }
    
    // Scheduled RPC
    var scheduledRpc by remember { mutableStateOf(Prefs[Prefs.SCHEDULED_RPC_ENABLED, false]) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.advanced_features),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = { BackButton { onBackPressed() } }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            // Auto-Start Section
            item {
                Subtitle(text = stringResource(R.string.auto_start_on_boot))
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.auto_start_on_boot),
                    description = stringResource(id = R.string.auto_start_on_boot_desc),
                    icon = Icons.Default.PlayArrow,
                    isChecked = autoStartEnabled,
                ) {
                    autoStartEnabled = !autoStartEnabled
                    Prefs[Prefs.AUTO_START_RPC_ON_BOOT] = autoStartEnabled
                }
            }
            
            item {
                AnimatedVisibility(visible = autoStartEnabled) {
                    SettingItem(
                        title = stringResource(id = R.string.choose_rpc),
                        description = when (autoStartType) {
                            "app_detection" -> stringResource(R.string.main_appDetection)
                            "media" -> stringResource(R.string.main_mediaRpc)
                            "custom" -> stringResource(R.string.main_customRpc)
                            "experimental" -> stringResource(R.string.main_experimentalRpc)
                            "vc_stay" -> stringResource(R.string.main_vcStay)
                            else -> stringResource(R.string.main_customRpc)
                        },
                        icon = Icons.Default.AutoMode
                    ) {
                        showAutoStartTypeDialog = true
                    }
                }
            }
            
            // Power & Performance Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Subtitle(text = stringResource(R.string.power_performance))
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.battery_saver_mode),
                    description = stringResource(id = R.string.battery_saver_mode_desc),
                    icon = Icons.Default.BatteryChargingFull,
                    isChecked = batterySaverEnabled,
                ) {
                    batterySaverEnabled = !batterySaverEnabled
                    Prefs[Prefs.BATTERY_SAVER_MODE] = batterySaverEnabled
                }
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.connection_status),
                    description = stringResource(id = R.string.connection_status_desc),
                    icon = Icons.Default.SignalCellularAlt,
                    isChecked = showConnectionStatus,
                ) {
                    showConnectionStatus = !showConnectionStatus
                    Prefs[Prefs.SHOW_CONNECTION_STATUS] = showConnectionStatus
                }
            }
            
            // Privacy Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Subtitle(text = stringResource(R.string.privacy))
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.invisible_mode),
                    description = stringResource(id = R.string.invisible_mode_desc),
                    icon = if (invisibleMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    isChecked = invisibleMode,
                ) {
                    invisibleMode = !invisibleMode
                    Prefs[Prefs.INVISIBLE_MODE] = invisibleMode
                    if (invisibleMode) {
                        Prefs[Prefs.CUSTOM_ACTIVITY_STATUS] = "invisible"
                    }
                }
            }
            
            // Automation Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Subtitle(text = stringResource(R.string.automation))
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.random_rpc_rotation),
                    description = stringResource(id = R.string.random_rpc_rotation_desc),
                    icon = Icons.Default.Shuffle,
                    isChecked = randomRotation,
                ) {
                    randomRotation = !randomRotation
                    Prefs[Prefs.RANDOM_RPC_ROTATION] = randomRotation
                }
            }
            
            item {
                AnimatedVisibility(visible = randomRotation) {
                    SettingItem(
                        title = stringResource(id = R.string.rotation_interval),
                        description = "$rotationInterval ${stringResource(id = R.string.rotation_interval_minutes)}",
                        icon = Icons.Default.Refresh
                    ) {
                        showRotationIntervalDialog = true
                    }
                }
            }
            
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.scheduled_rpc),
                    description = stringResource(id = R.string.scheduled_rpc_desc),
                    icon = Icons.Default.Schedule,
                    isChecked = scheduledRpc,
                ) {
                    scheduledRpc = !scheduledRpc
                    Prefs[Prefs.SCHEDULED_RPC_ENABLED] = scheduledRpc
                    Toast.makeText(context, context.getString(R.string.coming_soon_feature), Toast.LENGTH_SHORT).show()
                }
            }
            
            // History & Data Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Subtitle(text = stringResource(R.string.rpc_history))
            }
            
            item {
                SettingItem(
                    title = stringResource(id = R.string.rpc_history),
                    description = "${Prefs.getRpcHistory().size} ${stringResource(id = R.string.recently_used).lowercase()}",
                    icon = Icons.Default.History
                ) {
                    // Navigate to RPC history
                }
            }
            
            item {
                SettingItem(
                    title = stringResource(id = R.string.favorites),
                    description = "${Prefs.getFavorites().size} ${stringResource(id = R.string.favorites).lowercase()}",
                    icon = Icons.Default.Star
                ) {
                    // Navigate to favorites
                }
            }
            
            item {
                SettingItem(
                    title = stringResource(id = R.string.clear_history),
                    description = stringResource(id = R.string.no_history),
                    icon = Icons.Default.History
                ) {
                    Prefs.clearRpcHistory()
                    Toast.makeText(context, context.getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
                }
            }
            
            // Developer Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Subtitle(text = stringResource(R.string.developer_options))
            }
            
            item {
                SettingItem(
                    title = stringResource(id = R.string.debug_mode),
                    description = stringResource(id = R.string.debug_mode_desc),
                    icon = Icons.Default.Code
                ) {
                    Toast.makeText(context, context.getString(R.string.coming_soon_feature), Toast.LENGTH_SHORT).show()
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Auto-start type dialog
        if (showAutoStartTypeDialog) {
            AlertDialog(
                onDismissRequest = { showAutoStartTypeDialog = false },
                confirmButton = {},
                title = { Text(stringResource(R.string.choose_rpc)) },
                text = {
                    Column {
                        val rpcTypes = listOf(
                            "app_detection" to R.string.main_appDetection,
                            "media" to R.string.main_mediaRpc,
                            "custom" to R.string.main_customRpc,
                            "experimental" to R.string.main_experimentalRpc,
                            "vc_stay" to R.string.main_vcStay
                        )
                        rpcTypes.forEach { (type, labelRes) ->
                            SingleChoiceItem(
                                text = stringResource(labelRes),
                                selected = autoStartType == type
                            ) {
                                autoStartType = type
                                Prefs[Prefs.AUTO_START_RPC_TYPE] = type
                                showAutoStartTypeDialog = false
                            }
                        }
                    }
                }
            )
        }
        
        // Rotation interval dialog
        if (showRotationIntervalDialog) {
            val minutesText = stringResource(R.string.minutes)
            AlertDialog(
                onDismissRequest = { showRotationIntervalDialog = false },
                confirmButton = {},
                title = { Text(stringResource(R.string.rotation_interval)) },
                text = {
                    Column {
                        val intervals = listOf(5, 10, 15, 30, 60, 120)
                        intervals.forEach { interval ->
                            SingleChoiceItem(
                                text = "$interval $minutesText",
                                selected = rotationInterval == interval
                            ) {
                                rotationInterval = interval
                                Prefs[Prefs.RANDOM_RPC_INTERVAL] = interval
                                showRotationIntervalDialog = false
                            }
                        }
                    }
                }
            )
        }
    }
}
