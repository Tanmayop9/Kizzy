/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * TemplateProcessor.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.data.rpc

import android.media.MediaMetadata

/**
 * Template keys for customizing RPC display.
 * Inspired by PreMiD's template system for rich, customizable presence.
 */
class TemplateKeys {
    companion object {
        // Media template keys
        const val MEDIA_TITLE = "{{media_title}}"
        const val MEDIA_ARTIST = "{{media_artist}}"
        const val MEDIA_AUTHOR = "{{media_author}}"
        const val MEDIA_ALBUM = "{{media_album}}"
        const val MEDIA_ALBUM_ARTIST = "{{media_album_artist}}"
        
        // App template keys
        const val APP_NAME = "{{app_name}}"
        const val APP_ACTIVITY = "{{app_activity}}"
        const val APP_STATE = "{{app_state}}"
        
        // Prefix templates for better formatting
        const val LISTENING_TO = "Listening to {{app_name}}"
        const val WATCHING = "Watching {{app_name}}"
        const val PLAYING = "Playing {{app_name}}"
        const val BROWSING = "Browsing {{app_name}}"
        
        // All available template keys for UI display
        val ALL_KEYS = listOf(
            MEDIA_TITLE,
            MEDIA_ARTIST,
            MEDIA_AUTHOR,
            MEDIA_ALBUM,
            MEDIA_ALBUM_ARTIST,
            APP_NAME,
            APP_ACTIVITY,
            APP_STATE
        )
    }
}

/**
 * Processes template strings by replacing placeholders with actual values.
 * Supports both media metadata and app activity information.
 * 
 * Example usage:
 * - "{{media_title}} by {{media_artist}}" → "Song Name by Artist Name"
 * - "{{app_activity}}" → "Scrolling Feed"
 */
class TemplateProcessor(
    private val mediaMetadata: MediaMetadata? = null,
    private val mediaPlayerAppName: String? = null,
    private val mediaPlayerPackageName: String? = null,
    private val detectedAppInfo: CommonRpc? = null,
) {
    fun process(template: String?): String? {
        if (template.isNullOrBlank()) return null

        var result = template

        // Process media metadata if available (takes priority over app info)
        if (mediaMetadata != null && mediaPlayerAppName != null && mediaPlayerPackageName != null) {
            result = result
                .replace(
                    TemplateKeys.MEDIA_TITLE,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                )
                .replace(
                    TemplateKeys.MEDIA_ARTIST,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                )
                .replace(
                    TemplateKeys.MEDIA_AUTHOR,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_AUTHOR) ?: ""
                )
                .replace(
                    TemplateKeys.MEDIA_ALBUM,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
                )
                .replace(
                    TemplateKeys.MEDIA_ALBUM_ARTIST,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: ""
                )
                .replace(TemplateKeys.APP_NAME, mediaPlayerAppName)
        } else if (detectedAppInfo != null) {
            // Process app activity info if no media context
            result = result
                .replace(TemplateKeys.APP_NAME, detectedAppInfo.name)
                .replace(TemplateKeys.APP_ACTIVITY, detectedAppInfo.details ?: "")
                .replace(TemplateKeys.APP_STATE, detectedAppInfo.state ?: "")
        }

        // Remove unreplaced placeholders and clean up
        result = result.replace(
            Regex("\\{\\{(media|app)_[^}]+\\}\\}"), ""
        )
        
        // Trim and clean up extra spaces
        result = result.trim().replace(Regex("\\s+"), " ")

        // Return empty string if blank to maintain backwards compatibility
        return if (result.isBlank()) "" else result
    }
}
