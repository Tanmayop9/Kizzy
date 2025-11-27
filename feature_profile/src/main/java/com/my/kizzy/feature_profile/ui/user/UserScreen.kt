/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_profile.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.my.kizzy.domain.model.user.User
import com.my.kizzy.feature_profile.ui.component.Logout
import com.my.kizzy.feature_profile.ui.component.ProfileCard
import com.my.kizzy.feature_profile.ui.component.ProfileNetworkError
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.preference.PreferenceItem
import com.my.kizzy.ui.components.shimmer.AnimatedShimmer
import com.my.kizzy.ui.components.shimmer.ShimmerProfileCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    state: UserState,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var showEditBioDialog by remember { mutableStateOf(false) }
    var bioText by remember { mutableStateOf(Prefs[Prefs.USER_BIO, ""]) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // Edit Bio Dialog
    if (showEditBioDialog) {
        AlertDialog(
            onDismissRequest = { showEditBioDialog = false },
            title = { Text(stringResource(id = R.string.change_bio)) },
            text = {
                OutlinedTextField(
                    value = bioText,
                    onValueChange = { 
                        if (it.length <= 190) bioText = it 
                    },
                    label = { Text(stringResource(id = R.string.bio_max_chars)) },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    Prefs[Prefs.USER_BIO] = bioText
                    showEditBioDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = ctx.getString(R.string.profile_updated),
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBioDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Status Selection Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text(stringResource(id = R.string.change_status)) },
            text = {
                Column {
                    val statuses = listOf(
                        "online" to R.string.status_online,
                        "idle" to R.string.status_idle,
                        "dnd" to R.string.status_dnd,
                        "invisible" to R.string.status_invisible_offline
                    )
                    statuses.forEach { (status, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "online"] == status,
                                onClick = {
                                    Prefs[Prefs.CUSTOM_ACTIVITY_STATUS] = status
                                    showStatusDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = labelRes))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.user_profile)) },
                navigationIcon = { BackButton { onBackPressed() } })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                when (state) {
                    is UserState.Error -> {
                        ProfileNetworkError(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            error = (state.error)
                        )
                        ProfileCard(state.user)
                    }

                    UserState.Loading -> {
                        AnimatedShimmer {
                            ShimmerProfileCard(brush = it)
                        }
                    }

                    is UserState.LoadingCompleted -> {
                        ProfileCard(state.user)
                    }
                }
            }

            // Profile Management Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.edit_profile),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.change_bio),
                    description = bioText.ifEmpty { stringResource(id = R.string.enter_bio) },
                    icon = Icons.Default.Edit,
                    onClick = { showEditBioDialog = true }
                )
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.change_status),
                    description = Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "online"],
                    icon = Icons.Default.Person,
                    onClick = { showStatusDialog = true }
                )
            }

            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.advanced_features),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.rpc_history),
                    description = stringResource(id = R.string.rpc_history_desc),
                    icon = Icons.Default.History
                )
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.favorites),
                    description = "${Prefs.getFavorites().size} ${stringResource(id = R.string.favorites).lowercase()}",
                    icon = Icons.Default.Star
                )
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.statistics),
                    description = stringResource(id = R.string.total_uptime),
                    icon = Icons.Default.Settings
                )
            }

            // Logout Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Logout(
                    modifier = Modifier
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = ctx.getString(R.string.are_you_sure),
                            actionLabel = ctx.getString(R.string.yes),
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        ).run {
                            when (this) {
                                SnackbarResult.ActionPerformed -> try {
                                    val runtime = Runtime.getRuntime()
                                    runtime.exec("pm clear com.my.kizzy")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                SnackbarResult.Dismissed -> Unit
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

val fakeUser = User(
    accentColor = null,
    avatar = null,
    avatarDecoration = null,
    badges = null,
    banner = null,
    bannerColor = null,
    discriminator = "3050",
    id = null,
    publicFlags = null,
    username = "yzziK",
    special = null,
    verified = false,
    nitro = true,
    bio = "Hello 👋"
)
@Preview
@Composable
fun UserScreenPreview() {
    UserScreen(
        state = UserState.Loading,
        onBackPressed = {},
    )
}
@Preview
@Composable
fun UserScreenPreview2() {
    UserScreen(
        state = UserState.Error(
            error = "No Internet Connection",
            user = fakeUser
        ),
        onBackPressed = {}
    )
}
@Preview
@Composable
fun UserScreenPreview3() {
    UserScreen(
        state = UserState.LoadingCompleted(
            user = fakeUser
        ),
        onBackPressed = {}
    )
}