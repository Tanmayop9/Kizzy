/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * StreamOnVCScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_vc_stay

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
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
import com.my.kizzy.feature_rpc_base.services.StreamOnVCService
import com.my.kizzy.feature_rpc_base.services.VCStayService
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.SwitchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamOnVCScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var streamOnVCRunning by remember { mutableStateOf(AppUtils.streamOnVCRunning()) }
    var guildId by remember { mutableStateOf(Prefs[Prefs.STREAM_VC_GUILD_ID, ""]) }
    var channelId by remember { mutableStateOf(Prefs[Prefs.STREAM_VC_CHANNEL_ID, ""]) }
    var youtubeUrl by remember { mutableStateOf(Prefs[Prefs.STREAM_VC_YOUTUBE_URL, ""]) }
    var streamName by remember { mutableStateOf(Prefs[Prefs.STREAM_VC_STREAM_NAME, ""]) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    val canStartStream = guildId.isNotEmpty() && 
                         channelId.isNotEmpty() && 
                         youtubeUrl.isNotEmpty() && 
                         StreamOnVCService.isValidYoutubeUrl(youtubeUrl)

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(id = R.string.stream_vc_how_to)) },
            text = { Text(stringResource(id = R.string.stream_vc_instructions)) },
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
                        text = stringResource(id = R.string.main_streamOnVc),
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
                title = stringResource(id = R.string.enable_stream_vc),
                isChecked = streamOnVCRunning,
                enabled = canStartStream
            ) {
                streamOnVCRunning = !streamOnVCRunning
                when (streamOnVCRunning) {
                    true -> {
                        if (!StreamOnVCService.isValidYoutubeUrl(youtubeUrl)) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.stream_vc_invalid_url),
                                Toast.LENGTH_SHORT
                            ).show()
                            streamOnVCRunning = false
                            return@SwitchBar
                        }
                        
                        // Stop other services first
                        context.stopService(Intent(context, AppDetectionService::class.java))
                        context.stopService(Intent(context, CustomRpcService::class.java))
                        context.stopService(Intent(context, ExperimentalRpc::class.java))
                        context.stopService(Intent(context, MediaRpcService::class.java))
                        context.stopService(Intent(context, VCStayService::class.java))
                        
                        // Start Stream on VC service
                        val intent = Intent(context, StreamOnVCService::class.java).apply {
                            putExtra(StreamOnVCService.EXTRA_GUILD_ID, guildId)
                            putExtra(StreamOnVCService.EXTRA_CHANNEL_ID, channelId)
                            putExtra(StreamOnVCService.EXTRA_YOUTUBE_URL, youtubeUrl)
                            putExtra(StreamOnVCService.EXTRA_STREAM_NAME, streamName.ifEmpty { "YouTube" })
                        }
                        context.startService(intent)
                    }
                    false -> {
                        context.stopService(Intent(context, StreamOnVCService::class.java))
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
                            Prefs[Prefs.STREAM_VC_GUILD_ID] = it
                        },
                        label = { Text(stringResource(id = R.string.stream_vc_guild_id)) },
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
                            Prefs[Prefs.STREAM_VC_CHANNEL_ID] = it
                        },
                        label = { Text(stringResource(id = R.string.stream_vc_channel_id)) },
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
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = youtubeUrl,
                        onValueChange = { 
                            youtubeUrl = it
                            Prefs[Prefs.STREAM_VC_YOUTUBE_URL] = it
                        },
                        label = { Text(stringResource(id = R.string.stream_vc_youtube_url)) },
                        placeholder = { Text(stringResource(id = R.string.stream_vc_youtube_url_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        isError = youtubeUrl.isNotEmpty() && !StreamOnVCService.isValidYoutubeUrl(youtubeUrl),
                        supportingText = {
                            if (youtubeUrl.isNotEmpty() && !StreamOnVCService.isValidYoutubeUrl(youtubeUrl)) {
                                Text(
                                    text = stringResource(id = R.string.stream_vc_invalid_url),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = streamName,
                        onValueChange = { 
                            streamName = it
                            Prefs[Prefs.STREAM_VC_STREAM_NAME] = it
                        },
                        label = { Text(stringResource(id = R.string.stream_vc_stream_name)) },
                        placeholder = { Text(stringResource(id = R.string.stream_vc_stream_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = streamOnVCRunning) {
                        StreamingStatusCard(streamName = streamName.ifEmpty { "YouTube" })
                    }
                }
            }
        }
    }
}

@Composable
fun StreamingStatusCard(streamName: String) {
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
                text = stringResource(id = R.string.stream_vc_streaming) + ": $streamName",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
