/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppActivityDetector.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.data.get_current_data.app

import android.content.Context
import com.my.kizzy.data.rpc.CommonRpc
import com.my.kizzy.data.rpc.RpcImage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced app activity detector that provides more detailed activity information
 * for specific apps like Instagram, YouTube, Net Mirror, and PW.
 */
@Singleton
class AppActivityDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Instagram package name
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val INSTAGRAM_LITE_PACKAGE = "com.instagram.lite"

        // YouTube package names
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        const val YOUTUBE_MUSIC_PACKAGE = "com.google.android.apps.youtube.music"
        const val YOUTUBE_KIDS_PACKAGE = "com.google.android.apps.youtube.kids"
        const val YOUTUBE_TV_PACKAGE = "com.google.android.youtube.tv"

        // Net Mirror package name (common mirror apps)
        const val NET_MIRROR_PACKAGE = "com.nicnet.netmirror"

        // Physics Wallah (PW) package names
        const val PW_PACKAGE = "xyz.penpencil.physicswala"
        const val PW_PATHSHALA_PACKAGE = "xyz.penpencil.pathshala"

        // Activity types
        const val ACTIVITY_WATCHING = 3 // Watching
        const val ACTIVITY_PLAYING = 0 // Playing/Using
        const val ACTIVITY_LISTENING = 2 // Listening
    }

    /**
     * Map of package names to their display names and activity details
     */
    private val appActivityConfig = mapOf(
        INSTAGRAM_PACKAGE to AppConfig(
            appName = "Instagram",
            defaultActivity = "Browsing Instagram",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Reels", "Scrolling through Reels"),
                ActivityState("Viewing Stories", "Watching Stories"),
                ActivityState("Browsing Feed", "Scrolling the Feed"),
                ActivityState("Viewing Messages", "Checking DMs"),
                ActivityState("Exploring", "Discovering content"),
                ActivityState("Viewing Profile", "Checking Profile"),
                ActivityState("Posting Content", "Creating Post")
            )
        ),
        INSTAGRAM_LITE_PACKAGE to AppConfig(
            appName = "Instagram Lite",
            defaultActivity = "Browsing Instagram",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Reels", "Scrolling through Reels"),
                ActivityState("Viewing Stories", "Watching Stories"),
                ActivityState("Browsing Feed", "Scrolling the Feed"),
                ActivityState("Viewing Messages", "Checking DMs")
            )
        ),
        YOUTUBE_PACKAGE to AppConfig(
            appName = "YouTube",
            defaultActivity = "Watching YouTube",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Streaming content"),
                ActivityState("Watching Shorts", "Scrolling Shorts"),
                ActivityState("Browsing Home", "Exploring content"),
                ActivityState("Viewing Subscriptions", "Checking subscriptions"),
                ActivityState("Watching Live", "Watching Live Stream")
            )
        ),
        YOUTUBE_MUSIC_PACKAGE to AppConfig(
            appName = "YouTube Music",
            defaultActivity = "Listening to Music",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Enjoying tunes"),
                ActivityState("Exploring Music", "Discovering new music"),
                ActivityState("Playing Playlist", "Vibing to playlist")
            )
        ),
        YOUTUBE_KIDS_PACKAGE to AppConfig(
            appName = "YouTube Kids",
            defaultActivity = "Watching YouTube Kids",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Enjoying kid-friendly content"),
                ActivityState("Exploring", "Discovering content")
            )
        ),
        YOUTUBE_TV_PACKAGE to AppConfig(
            appName = "YouTube TV",
            defaultActivity = "Watching YouTube TV",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Live TV", "Streaming live"),
                ActivityState("Watching Recordings", "Watching recorded content")
            )
        ),
        NET_MIRROR_PACKAGE to AppConfig(
            appName = "Net Mirror",
            defaultActivity = "Screen Mirroring",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Screen Mirroring", "Casting to display"),
                ActivityState("Casting Content", "Sharing screen"),
                ActivityState("Streaming", "Streaming content")
            )
        ),
        PW_PACKAGE to AppConfig(
            appName = "Physics Wallah",
            defaultActivity = "Learning on PW",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Attending Class", "Watching live class"),
                ActivityState("Watching Lecture", "Studying recorded lectures"),
                ActivityState("Taking Notes", "Learning actively"),
                ActivityState("Solving Problems", "Practicing questions"),
                ActivityState("Browsing Courses", "Exploring courses"),
                ActivityState("Taking Test", "Attempting assessment")
            )
        ),
        PW_PATHSHALA_PACKAGE to AppConfig(
            appName = "PW Pathshala",
            defaultActivity = "Learning on PW Pathshala",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Attending Class", "Watching live class"),
                ActivityState("Watching Lecture", "Studying recorded lectures"),
                ActivityState("Practicing", "Solving problems")
            )
        )
    )

    /**
     * Get enhanced activity information for the given package
     */
    fun getEnhancedActivity(packageName: String, appName: String): CommonRpc {
        val config = appActivityConfig[packageName]

        return if (config != null) {
            // Use the default activity for the app
            val activity = config.activities.firstOrNull() ?: ActivityState(config.defaultActivity, null)

            CommonRpc(
                name = config.appName,
                type = config.activityType,
                details = activity.details,
                state = activity.state,
                largeImage = RpcImage.ApplicationIcon(packageName, context),
                packageName = packageName
            )
        } else {
            // Return standard app info for non-configured apps
            CommonRpc(
                name = appName,
                largeImage = RpcImage.ApplicationIcon(packageName, context),
                packageName = packageName
            )
        }
    }

    /**
     * Check if this app has enhanced activity detection
     */
    fun hasEnhancedDetection(packageName: String): Boolean {
        return appActivityConfig.containsKey(packageName)
    }

    /**
     * Get all supported packages for enhanced detection
     */
    fun getSupportedPackages(): Set<String> {
        return appActivityConfig.keys
    }

    /**
     * Get app configuration for the given package
     */
    fun getAppConfig(packageName: String): AppConfig? {
        return appActivityConfig[packageName]
    }
}

/**
 * Configuration for app-specific activity detection
 */
data class AppConfig(
    val appName: String,
    val defaultActivity: String,
    val activityType: Int,
    val activities: List<ActivityState>
)

/**
 * Represents a specific activity state within an app
 */
data class ActivityState(
    val details: String,
    val state: String?
)
