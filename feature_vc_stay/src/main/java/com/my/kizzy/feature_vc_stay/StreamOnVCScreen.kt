/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * StreamOnVCScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of Tanmay
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_vc_stay

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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

    // Get video info for preview
    val videoInfo = remember(youtubeUrl) {
        if (StreamOnVCService.isValidStreamUrl(youtubeUrl)) {
            StreamOnVCService.extractVideoInfo(youtubeUrl)
        } else null
    }

    val canStartStream = guildId.isNotEmpty() && 
                         channelId.isNotEmpty() && 
                         youtubeUrl.isNotEmpty() && 
                         StreamOnVCService.isValidStreamUrl(youtubeUrl)

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
                    Text(stringResource(id = R.string.stream_vc_how_to)) 
                }
            },
            text = { Text(stringResource(id = R.string.stream_vc_instructions)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SwitchBar(
                    title = stringResource(id = R.string.enable_stream_vc),
                    isChecked = streamOnVCRunning,
                    enabled = canStartStream
                ) {
                    streamOnVCRunning = !streamOnVCRunning
                    when (streamOnVCRunning) {
                        true -> {
                            if (!StreamOnVCService.isValidStreamUrl(youtubeUrl)) {
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
                                putExtra(StreamOnVCService.EXTRA_STREAM_NAME, streamName.ifEmpty { videoInfo?.defaultName ?: "YouTube" })
                            }
                            context.startService(intent)
                        }
                        false -> {
                            context.stopService(Intent(context, StreamOnVCService::class.java))
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = streamOnVCRunning,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    StreamingStatusCard(
                        streamName = streamName.ifEmpty { videoInfo?.defaultName ?: "YouTube" },
                        youtubeUrl = youtubeUrl,
                        videoInfo = videoInfo
                    )
                }
            }

            // Video Preview Card (when not streaming but URL is valid)
            item {
                AnimatedVisibility(
                    visible = !streamOnVCRunning && videoInfo != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    videoInfo?.let {
                        VideoPreviewCard(videoInfo = it)
                    }
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
                            text = stringResource(id = R.string.server_configuration),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        StyledTextField(
                            value = guildId,
                            onValueChange = { 
                                guildId = it
                                Prefs[Prefs.STREAM_VC_GUILD_ID] = it
                            },
                            label = stringResource(id = R.string.stream_vc_guild_id),
                            icon = Icons.Default.Forum,
                            keyboardType = KeyboardType.Number,
                            enabled = !streamOnVCRunning
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        StyledTextField(
                            value = channelId,
                            onValueChange = { 
                                channelId = it
                                Prefs[Prefs.STREAM_VC_CHANNEL_ID] = it
                            },
                            label = stringResource(id = R.string.stream_vc_channel_id),
                            icon = Icons.Default.Headset,
                            keyboardType = KeyboardType.Number,
                            enabled = !streamOnVCRunning
                        )
                    }
                }
            }

            item {
                // Stream Settings Card
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
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.stream_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        StyledTextField(
                            value = youtubeUrl,
                            onValueChange = { 
                                youtubeUrl = it
                                Prefs[Prefs.STREAM_VC_YOUTUBE_URL] = it
                            },
                            label = stringResource(id = R.string.stream_vc_youtube_url),
                            placeholder = stringResource(id = R.string.stream_vc_youtube_url_hint),
                            icon = Icons.Default.VideoLibrary,
                            isError = youtubeUrl.isNotEmpty() && !StreamOnVCService.isValidStreamUrl(youtubeUrl),
                            errorMessage = if (youtubeUrl.isNotEmpty() && !StreamOnVCService.isValidStreamUrl(youtubeUrl)) 
                                stringResource(id = R.string.stream_vc_invalid_url) else null,
                            enabled = !streamOnVCRunning
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        StyledTextField(
                            value = streamName,
                            onValueChange = { 
                                streamName = it
                                Prefs[Prefs.STREAM_VC_STREAM_NAME] = it
                            },
                            label = stringResource(id = R.string.stream_vc_stream_name),
                            placeholder = stringResource(id = R.string.stream_vc_stream_name_hint),
                            icon = Icons.Default.Videocam,
                            enabled = !streamOnVCRunning
                        )
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
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        enabled = enabled,
        supportingText = if (errorMessage != null) {
            {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun VideoPreviewCard(videoInfo: StreamOnVCService.Companion.VideoInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.stream_vc_video_preview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Video Thumbnail
            if (videoInfo.thumbnailUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = videoInfo.thumbnailUrl,
                        contentDescription = "Video thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Play button overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play",
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = CircleShape
                                ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Platform badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = when (videoInfo.platform) {
                                    "YouTube" -> Color(0xFFFF0000)
                                    "Twitch" -> Color(0xFF9146FF)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = videoInfo.platform,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // No thumbnail available placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = videoInfo.platform,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Video Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = videoInfo.defaultName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (videoInfo.videoId != null) {
                        Text(
                            text = "ID: ${videoInfo.videoId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.stream_vc_ready_to_stream),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingStatusCard(
    streamName: String, 
    youtubeUrl: String = "",
    videoInfo: StreamOnVCService.Companion.VideoInfo? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live indicator with animation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFF0000),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF0000)
                        )
                        Text(
                            text = stringResource(id = R.string.stream_vc_streaming),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = streamName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Video info section
            if (videoInfo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.stream_vc_platform),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = videoInfo.platform,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (videoInfo.videoId != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(id = R.string.stream_vc_video_id),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                text = videoInfo.videoId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            if (youtubeUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = youtubeUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
