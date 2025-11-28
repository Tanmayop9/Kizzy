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
 * for specific apps. Inspired by PreMiD's rich presence approach.
 * 
 * This detector provides:
 * - Proper activity types (Playing, Listening, Watching, Competing)
 * - Rich details and states for better Discord presence display
 * - Support for popular social media, streaming, gaming, and utility apps
 */
@Singleton
class AppActivityDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // ==================== SOCIAL MEDIA APPS ====================
        // Instagram
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val INSTAGRAM_LITE_PACKAGE = "com.instagram.lite"
        
        // Facebook
        const val FACEBOOK_PACKAGE = "com.facebook.katana"
        const val FACEBOOK_LITE_PACKAGE = "com.facebook.lite"
        const val MESSENGER_PACKAGE = "com.facebook.orca"
        const val MESSENGER_LITE_PACKAGE = "com.facebook.mlite"
        
        // Twitter/X
        const val TWITTER_PACKAGE = "com.twitter.android"
        const val TWITTER_LITE_PACKAGE = "com.twitter.android.lite"
        
        // Snapchat
        const val SNAPCHAT_PACKAGE = "com.snapchat.android"
        
        // Reddit
        const val REDDIT_PACKAGE = "com.reddit.frontpage"
        
        // Pinterest
        const val PINTEREST_PACKAGE = "com.pinterest"
        
        // LinkedIn
        const val LINKEDIN_PACKAGE = "com.linkedin.android"
        
        // Threads
        const val THREADS_PACKAGE = "com.instagram.barcelona"
        
        // BeReal
        const val BEREAL_PACKAGE = "com.bereal.ft"
        
        // ==================== VIDEO STREAMING APPS ====================
        // YouTube
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        const val YOUTUBE_MUSIC_PACKAGE = "com.google.android.apps.youtube.music"
        const val YOUTUBE_KIDS_PACKAGE = "com.google.android.apps.youtube.kids"
        const val YOUTUBE_TV_PACKAGE = "com.google.android.youtube.tv"
        
        // Netflix
        const val NETFLIX_PACKAGE = "com.netflix.mediaclient"
        
        // Amazon Prime Video
        const val PRIME_VIDEO_PACKAGE = "com.amazon.avod.thirdpartyclient"
        const val PRIME_VIDEO_PACKAGE_ALT = "com.amazon.avod"
        
        // Disney+
        const val DISNEY_PLUS_PACKAGE = "com.disney.disneyplus"
        
        // HBO Max
        const val HBO_MAX_PACKAGE = "com.hbo.hbonow"
        
        // Hotstar
        const val HOTSTAR_PACKAGE = "in.startv.hotstar"
        
        // Twitch
        const val TWITCH_PACKAGE = "tv.twitch.android.app"
        
        // Kick
        const val KICK_PACKAGE = "com.kick.mobile"
        
        // ==================== MUSIC STREAMING APPS ====================
        // Spotify
        const val SPOTIFY_PACKAGE = "com.spotify.music"
        const val SPOTIFY_LITE_PACKAGE = "com.spotify.lite"
        
        // Apple Music
        const val APPLE_MUSIC_PACKAGE = "com.apple.android.music"
        
        // Amazon Music
        const val AMAZON_MUSIC_PACKAGE = "com.amazon.mp3"
        
        // SoundCloud
        const val SOUNDCLOUD_PACKAGE = "com.soundcloud.android"
        
        // Deezer
        const val DEEZER_PACKAGE = "deezer.android.app"
        
        // Tidal
        const val TIDAL_PACKAGE = "com.aspiro.tidal"
        
        // JioSaavn
        const val JIOSAAVN_PACKAGE = "com.jio.media.jiobeats"
        
        // Gaana
        const val GAANA_PACKAGE = "com.gaana"
        
        // Wynk Music
        const val WYNK_PACKAGE = "com.bsbportal.music"
        
        // ==================== SHORT VIDEO APPS ====================
        // TikTok
        const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
        const val TIKTOK_LITE_PACKAGE = "com.ss.android.ugc.trill"
        
        // Josh
        const val JOSH_PACKAGE = "com.myjosh"
        
        // Moj
        const val MOJ_PACKAGE = "in.dailyhunt.dh.moj"
        
        // MX TakaTak
        const val MX_TAKATAK_PACKAGE = "com.mxtech.videoplayer.takatak"
        
        // ==================== MESSAGING APPS ====================
        // WhatsApp
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        
        // Telegram
        const val TELEGRAM_PACKAGE = "org.telegram.messenger"
        const val TELEGRAM_X_PACKAGE = "org.thunderdog.challegram"
        
        // Signal
        const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
        
        // Discord
        const val DISCORD_PACKAGE = "com.discord"
        
        // Slack
        const val SLACK_PACKAGE = "com.Slack"
        
        // ==================== GAMING APPS ====================
        // BGMI/PUBG Mobile
        const val BGMI_PACKAGE = "com.pubg.imobile"
        const val PUBG_MOBILE_PACKAGE = "com.tencent.ig"
        
        // Free Fire
        const val FREE_FIRE_PACKAGE = "com.dts.freefireth"
        const val FREE_FIRE_MAX_PACKAGE = "com.dts.freefiremax"
        
        // Call of Duty Mobile
        const val COD_MOBILE_PACKAGE = "com.activision.callofduty.shooter"
        
        // Valorant Mobile
        const val VALORANT_MOBILE_PACKAGE = "com.riotgames.valorantmobile"
        
        // Clash of Clans
        const val CLASH_OF_CLANS_PACKAGE = "com.supercell.clashofclans"
        
        // Clash Royale
        const val CLASH_ROYALE_PACKAGE = "com.supercell.clashroyale"
        
        // Brawl Stars
        const val BRAWL_STARS_PACKAGE = "com.supercell.brawlstars"
        
        // Genshin Impact
        const val GENSHIN_IMPACT_PACKAGE = "com.miHoYo.GenshinImpact"
        
        // Mobile Legends
        const val MOBILE_LEGENDS_PACKAGE = "com.mobile.legends"
        
        // Among Us
        const val AMONG_US_PACKAGE = "com.innersloth.spacemafia"
        
        // Minecraft
        const val MINECRAFT_PACKAGE = "com.mojang.minecraftpe"
        
        // Roblox
        const val ROBLOX_PACKAGE = "com.roblox.client"
        
        // Subway Surfers
        const val SUBWAY_SURFERS_PACKAGE = "com.kiloo.subwaysurf"
        
        // Candy Crush
        const val CANDY_CRUSH_PACKAGE = "com.king.candycrushsaga"
        
        // ==================== UTILITY/PRODUCTIVITY APPS ====================
        // Net Mirror
        const val NET_MIRROR_PACKAGE = "com.nicnet.netmirror"
        
        // Google Chrome
        const val CHROME_PACKAGE = "com.android.chrome"
        
        // Microsoft Edge
        const val EDGE_PACKAGE = "com.microsoft.emmx"
        
        // Firefox
        const val FIREFOX_PACKAGE = "org.mozilla.firefox"
        
        // ==================== EDUCATION APPS ====================
        // Physics Wallah
        const val PW_PACKAGE = "xyz.penpencil.physicswala"
        const val PW_PATHSHALA_PACKAGE = "xyz.penpencil.pathshala"
        
        // Unacademy
        const val UNACADEMY_PACKAGE = "com.unacademyapp"
        
        // BYJU'S
        const val BYJUS_PACKAGE = "com.byjus.thelearningapp"
        
        // Vedantu
        const val VEDANTU_PACKAGE = "com.vedantu"
        
        // Doubtnut
        const val DOUBTNUT_PACKAGE = "com.doubtnutapp"
        
        // Khan Academy
        const val KHAN_ACADEMY_PACKAGE = "org.khanacademy.android"
        
        // Coursera
        const val COURSERA_PACKAGE = "org.coursera.android"
        
        // Udemy
        const val UDEMY_PACKAGE = "com.udemy.android"
        
        // ==================== READING/NEWS APPS ====================
        // Kindle
        const val KINDLE_PACKAGE = "com.amazon.kindle"
        
        // Google News
        const val GOOGLE_NEWS_PACKAGE = "com.google.android.apps.magazines"
        
        // Inshorts
        const val INSHORTS_PACKAGE = "com.nis.app"
        
        // ==================== FITNESS APPS ====================
        // Strava
        const val STRAVA_PACKAGE = "com.strava"
        
        // Nike Run Club
        const val NIKE_RUN_PACKAGE = "com.nike.plusgps"

        // Activity types (Discord Activity Types)
        const val ACTIVITY_PLAYING = 0    // Playing
        const val ACTIVITY_STREAMING = 1  // Streaming
        const val ACTIVITY_LISTENING = 2  // Listening to
        const val ACTIVITY_WATCHING = 3   // Watching
        const val ACTIVITY_COMPETING = 5  // Competing in
    }

    /**
     * Map of package names to their display names and activity details
     * Inspired by PreMiD's rich presence configurations
     */
    private val appActivityConfig = mapOf(
        // ==================== SOCIAL MEDIA ====================
        INSTAGRAM_PACKAGE to AppConfig(
            appName = "Instagram",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling Feed", "Checking out posts"),
                ActivityState("Watching Reels", "Exploring short videos"),
                ActivityState("Viewing Stories", "Catching up with friends"),
                ActivityState("Browsing Explore", "Discovering new content"),
                ActivityState("Checking DMs", "In conversations"),
                ActivityState("Viewing Profiles", "Stalking... I mean exploring"),
                ActivityState("Creating Content", "Making something cool")
            )
        ),
        INSTAGRAM_LITE_PACKAGE to AppConfig(
            appName = "Instagram Lite",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling Feed", "Checking out posts"),
                ActivityState("Watching Reels", "Exploring short videos"),
                ActivityState("Viewing Stories", "Catching up with friends"),
                ActivityState("Checking DMs", "In conversations")
            )
        ),
        THREADS_PACKAGE to AppConfig(
            appName = "Threads",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading Threads", "Exploring conversations"),
                ActivityState("Posting", "Sharing thoughts"),
                ActivityState("Browsing Feed", "Catching up")
            )
        ),
        FACEBOOK_PACKAGE to AppConfig(
            appName = "Facebook",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling Feed", "Catching up on news"),
                ActivityState("Watching Videos", "In the video section"),
                ActivityState("Viewing Stories", "Checking stories"),
                ActivityState("In Marketplace", "Shopping around"),
                ActivityState("Reading Groups", "Engaging with communities"),
                ActivityState("Checking Notifications", "Staying updated")
            )
        ),
        FACEBOOK_LITE_PACKAGE to AppConfig(
            appName = "Facebook Lite",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling Feed", "Catching up on news"),
                ActivityState("Viewing Stories", "Checking stories"),
                ActivityState("In Messenger", "Chatting")
            )
        ),
        MESSENGER_PACKAGE to AppConfig(
            appName = "Messenger",
            defaultActivity = "Chatting",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Chatting", "In conversation"),
                ActivityState("In Video Call", "Face to face"),
                ActivityState("In Voice Call", "On a call"),
                ActivityState("Viewing Stories", "Checking stories"),
                ActivityState("Browsing Chats", "Catching up")
            )
        ),
        MESSENGER_LITE_PACKAGE to AppConfig(
            appName = "Messenger Lite",
            defaultActivity = "Chatting",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Chatting", "In conversation"),
                ActivityState("Browsing Chats", "Catching up")
            )
        ),
        TWITTER_PACKAGE to AppConfig(
            appName = "X (Twitter)",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading Timeline", "Scrolling through tweets"),
                ActivityState("Viewing Trending", "Checking what's hot"),
                ActivityState("In Spaces", "Listening to conversations"),
                ActivityState("Posting", "Sharing thoughts"),
                ActivityState("Viewing Profile", "Exploring"),
                ActivityState("Reading Threads", "Deep in a thread")
            )
        ),
        TWITTER_LITE_PACKAGE to AppConfig(
            appName = "X Lite",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading Timeline", "Scrolling through tweets"),
                ActivityState("Posting", "Sharing thoughts")
            )
        ),
        SNAPCHAT_PACKAGE to AppConfig(
            appName = "Snapchat",
            defaultActivity = "Snapping",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Sending Snaps", "Staying connected"),
                ActivityState("Viewing Stories", "Catching up"),
                ActivityState("Chatting", "In conversation"),
                ActivityState("On Spotlight", "Watching content"),
                ActivityState("Using Lenses", "Having fun with filters"),
                ActivityState("Viewing Map", "Checking Snap Map")
            )
        ),
        REDDIT_PACKAGE to AppConfig(
            appName = "Reddit",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Browsing Home", "On the front page"),
                ActivityState("In a Subreddit", "Deep in a community"),
                ActivityState("Reading Comments", "In the discussion"),
                ActivityState("Watching Videos", "Scrolling r/all"),
                ActivityState("Posting", "Contributing content"),
                ActivityState("Viewing Saved", "Checking saved posts")
            )
        ),
        PINTEREST_PACKAGE to AppConfig(
            appName = "Pinterest",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Exploring Pins", "Getting inspired"),
                ActivityState("Creating Boards", "Organizing ideas"),
                ActivityState("Saving Pins", "Collecting inspiration"),
                ActivityState("Searching Ideas", "Finding something new")
            )
        ),
        LINKEDIN_PACKAGE to AppConfig(
            appName = "LinkedIn",
            defaultActivity = "Networking",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Browsing Feed", "Staying professional"),
                ActivityState("Viewing Jobs", "Exploring opportunities"),
                ActivityState("Messaging", "Connecting with professionals"),
                ActivityState("Viewing Profiles", "Networking"),
                ActivityState("Reading Articles", "Learning something new")
            )
        ),
        BEREAL_PACKAGE to AppConfig(
            appName = "BeReal",
            defaultActivity = "Being Real",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Posting BeReal", "Capturing the moment"),
                ActivityState("Viewing Friends", "Seeing what's up"),
                ActivityState("Reacting", "Engaging with friends")
            )
        ),
        
        // ==================== VIDEO STREAMING ====================
        YOUTUBE_PACKAGE to AppConfig(
            appName = "YouTube",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Entertainment time"),
                ActivityState("Watching Shorts", "Quick content fix"),
                ActivityState("Browsing Home", "Finding something to watch"),
                ActivityState("Checking Subscriptions", "Catching up"),
                ActivityState("Watching Live", "Tuned into a livestream"),
                ActivityState("In Premiere", "Watching with others")
            )
        ),
        YOUTUBE_MUSIC_PACKAGE to AppConfig(
            appName = "YouTube Music",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Vibing to tunes"),
                ActivityState("Exploring", "Discovering new music"),
                ActivityState("Playing Playlist", "On a music journey"),
                ActivityState("Radio Mode", "Endless music")
            )
        ),
        YOUTUBE_KIDS_PACKAGE to AppConfig(
            appName = "YouTube Kids",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Family-friendly content"),
                ActivityState("Exploring", "Finding fun videos")
            )
        ),
        YOUTUBE_TV_PACKAGE to AppConfig(
            appName = "YouTube TV",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Live TV", "Tuned in live"),
                ActivityState("Watching DVR", "Catching up on recordings")
            )
        ),
        NETFLIX_PACKAGE to AppConfig(
            appName = "Netflix",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching a Show", "Binge time"),
                ActivityState("Watching a Movie", "Movie night"),
                ActivityState("Browsing", "Finding something to watch"),
                ActivityState("Continuing", "Picking up where I left off")
            )
        ),
        PRIME_VIDEO_PACKAGE to AppConfig(
            appName = "Prime Video",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching a Show", "Prime entertainment"),
                ActivityState("Watching a Movie", "Cinema time"),
                ActivityState("Browsing", "Exploring the catalog")
            )
        ),
        PRIME_VIDEO_PACKAGE_ALT to AppConfig(
            appName = "Prime Video",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching a Show", "Prime entertainment"),
                ActivityState("Watching a Movie", "Cinema time"),
                ActivityState("Browsing", "Exploring the catalog")
            )
        ),
        DISNEY_PLUS_PACKAGE to AppConfig(
            appName = "Disney+",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Disney", "Magic on screen"),
                ActivityState("Watching Marvel", "Superhero time"),
                ActivityState("Watching Star Wars", "In a galaxy far away"),
                ActivityState("Browsing", "Finding the next adventure")
            )
        ),
        HBO_MAX_PACKAGE to AppConfig(
            appName = "HBO Max",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching HBO", "Premium content"),
                ActivityState("Browsing", "Exploring shows and movies")
            )
        ),
        HOTSTAR_PACKAGE to AppConfig(
            appName = "Disney+ Hotstar",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Shows", "Entertainment mode"),
                ActivityState("Watching Movies", "Movie time"),
                ActivityState("Watching Sports", "Live sports action"),
                ActivityState("Watching Live", "Tuned in live")
            )
        ),
        TWITCH_PACKAGE to AppConfig(
            appName = "Twitch",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Stream", "Live entertainment"),
                ActivityState("Chatting", "Engaging with community"),
                ActivityState("Browsing", "Finding streams"),
                ActivityState("Following Channels", "Checking favorites")
            )
        ),
        KICK_PACKAGE to AppConfig(
            appName = "Kick",
            defaultActivity = "Watching",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Stream", "Live on Kick"),
                ActivityState("Browsing", "Finding content"),
                ActivityState("Chatting", "In the chat")
            )
        ),
        
        // ==================== MUSIC STREAMING ====================
        SPOTIFY_PACKAGE to AppConfig(
            appName = "Spotify",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Vibing"),
                ActivityState("Playing Playlist", "Curated vibes"),
                ActivityState("Exploring", "Discovering new music"),
                ActivityState("Listening to Podcast", "Podcast time"),
                ActivityState("Radio Mode", "Endless tunes")
            )
        ),
        SPOTIFY_LITE_PACKAGE to AppConfig(
            appName = "Spotify Lite",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Vibing"),
                ActivityState("Playing Playlist", "Curated vibes")
            )
        ),
        APPLE_MUSIC_PACKAGE to AppConfig(
            appName = "Apple Music",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Apple tunes"),
                ActivityState("Exploring", "Discovering music"),
                ActivityState("Playing Radio", "Apple Radio")
            )
        ),
        AMAZON_MUSIC_PACKAGE to AppConfig(
            appName = "Amazon Music",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Prime tunes"),
                ActivityState("Playing Playlist", "Curated music")
            )
        ),
        SOUNDCLOUD_PACKAGE to AppConfig(
            appName = "SoundCloud",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Indie vibes"),
                ActivityState("Discovering", "Finding new artists"),
                ActivityState("Playing Playlist", "Curated tracks")
            )
        ),
        DEEZER_PACKAGE to AppConfig(
            appName = "Deezer",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Deezer vibes"),
                ActivityState("Exploring", "Finding new music")
            )
        ),
        TIDAL_PACKAGE to AppConfig(
            appName = "Tidal",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Hi-Fi quality"),
                ActivityState("Exploring", "Discovering music")
            )
        ),
        JIOSAAVN_PACKAGE to AppConfig(
            appName = "JioSaavn",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Desi tunes"),
                ActivityState("Playing Playlist", "Curated music"),
                ActivityState("Listening to Podcast", "Podcast time")
            )
        ),
        GAANA_PACKAGE to AppConfig(
            appName = "Gaana",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Gaana vibes"),
                ActivityState("Playing Playlist", "Curated tracks")
            )
        ),
        WYNK_PACKAGE to AppConfig(
            appName = "Wynk Music",
            defaultActivity = "Listening",
            activityType = ACTIVITY_LISTENING,
            activities = listOf(
                ActivityState("Listening to Music", "Wynk tunes"),
                ActivityState("Playing Playlist", "Music journey")
            )
        ),
        
        // ==================== SHORT VIDEO ====================
        TIKTOK_PACKAGE to AppConfig(
            appName = "TikTok",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling FYP", "For You page vibes"),
                ActivityState("Watching Videos", "Entertainment mode"),
                ActivityState("Going Live", "Streaming live"),
                ActivityState("Watching Live", "Tuned into a stream"),
                ActivityState("Creating Content", "Making videos"),
                ActivityState("Browsing Following", "Checking updates")
            )
        ),
        TIKTOK_LITE_PACKAGE to AppConfig(
            appName = "TikTok Lite",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Scrolling FYP", "For You page vibes"),
                ActivityState("Watching Videos", "Entertainment mode")
            )
        ),
        JOSH_PACKAGE to AppConfig(
            appName = "Josh",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Short video time"),
                ActivityState("Creating Content", "Making videos")
            )
        ),
        MOJ_PACKAGE to AppConfig(
            appName = "Moj",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Desi content"),
                ActivityState("Creating Content", "Making videos")
            )
        ),
        MX_TAKATAK_PACKAGE to AppConfig(
            appName = "MX TakaTak",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Videos", "Short video fun"),
                ActivityState("Creating Content", "Making videos")
            )
        ),
        
        // ==================== MESSAGING ====================
        WHATSAPP_PACKAGE to AppConfig(
            appName = "WhatsApp",
            defaultActivity = "Messaging",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Chatting", "In conversation"),
                ActivityState("In Group Chat", "Group discussion"),
                ActivityState("Viewing Status", "Checking updates"),
                ActivityState("On Call", "Voice/Video call"),
                ActivityState("In Community", "Community updates")
            )
        ),
        WHATSAPP_BUSINESS_PACKAGE to AppConfig(
            appName = "WhatsApp Business",
            defaultActivity = "Working",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Managing Business", "Professional mode"),
                ActivityState("Chatting with Clients", "Customer service"),
                ActivityState("Updating Catalog", "Business updates")
            )
        ),
        TELEGRAM_PACKAGE to AppConfig(
            appName = "Telegram",
            defaultActivity = "Messaging",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Chatting", "In conversation"),
                ActivityState("In Channel", "Following updates"),
                ActivityState("In Group", "Group discussion"),
                ActivityState("Browsing Stickers", "Finding reactions"),
                ActivityState("Using Bots", "Bot interaction")
            )
        ),
        TELEGRAM_X_PACKAGE to AppConfig(
            appName = "Telegram X",
            defaultActivity = "Messaging",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Chatting", "In conversation"),
                ActivityState("In Channel", "Following updates"),
                ActivityState("In Group", "Group discussion")
            )
        ),
        SIGNAL_PACKAGE to AppConfig(
            appName = "Signal",
            defaultActivity = "Messaging",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Secure Chatting", "Private conversation"),
                ActivityState("In Group", "Group discussion")
            )
        ),
        DISCORD_PACKAGE to AppConfig(
            appName = "Discord",
            defaultActivity = "Chatting",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Server", "Community vibes"),
                ActivityState("In Voice Channel", "Hanging out"),
                ActivityState("DMing Friends", "Private chat"),
                ActivityState("Streaming", "Screen sharing"),
                ActivityState("In Stage", "Listening to stage")
            )
        ),
        SLACK_PACKAGE to AppConfig(
            appName = "Slack",
            defaultActivity = "Working",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Workspace", "Team collaboration"),
                ActivityState("In Channel", "Discussion"),
                ActivityState("In Huddle", "Quick meeting"),
                ActivityState("DMing", "Direct message")
            )
        ),
        
        // ==================== GAMING ====================
        BGMI_PACKAGE to AppConfig(
            appName = "BGMI",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle Royale", "Winner Winner Chicken Dinner"),
                ActivityState("In TDM", "Team Deathmatch"),
                ActivityState("In Lobby", "Waiting for squad"),
                ActivityState("Ranking Up", "Grinding ranks")
            )
        ),
        PUBG_MOBILE_PACKAGE to AppConfig(
            appName = "PUBG Mobile",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle Royale", "Survival mode"),
                ActivityState("In TDM", "Team Deathmatch"),
                ActivityState("In Lobby", "Ready to drop")
            )
        ),
        FREE_FIRE_PACKAGE to AppConfig(
            appName = "Free Fire",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle", "Booyah time"),
                ActivityState("In Clash Squad", "Tactical combat"),
                ActivityState("In Lobby", "Preparing for battle")
            )
        ),
        FREE_FIRE_MAX_PACKAGE to AppConfig(
            appName = "Free Fire MAX",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle", "Booyah time"),
                ActivityState("In Clash Squad", "Tactical combat"),
                ActivityState("In Lobby", "Preparing for battle")
            )
        ),
        COD_MOBILE_PACKAGE to AppConfig(
            appName = "Call of Duty: Mobile",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle Royale", "Survival mode"),
                ActivityState("In Multiplayer", "Fast-paced action"),
                ActivityState("In Ranked", "Climbing the ladder"),
                ActivityState("In Lobby", "Ready for battle")
            )
        ),
        VALORANT_MOBILE_PACKAGE to AppConfig(
            appName = "Valorant Mobile",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Match", "Tactical shooter"),
                ActivityState("In Competitive", "Ranked grind"),
                ActivityState("Practicing", "Training mode")
            )
        ),
        CLASH_OF_CLANS_PACKAGE to AppConfig(
            appName = "Clash of Clans",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Raiding", "Attacking bases"),
                ActivityState("Building Base", "Upgrading village"),
                ActivityState("In Clan War", "Clan battles"),
                ActivityState("In Clan Games", "Completing challenges")
            )
        ),
        CLASH_ROYALE_PACKAGE to AppConfig(
            appName = "Clash Royale",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Battle", "Clash time"),
                ActivityState("In Ladder", "Trophy pushing"),
                ActivityState("In Challenge", "Special event"),
                ActivityState("In War", "Clan War battles")
            )
        ),
        BRAWL_STARS_PACKAGE to AppConfig(
            appName = "Brawl Stars",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Brawling", "Fast-paced battles"),
                ActivityState("In Ranked", "Power League"),
                ActivityState("In Club League", "Club battles"),
                ActivityState("In Special Event", "Event mode")
            )
        ),
        GENSHIN_IMPACT_PACKAGE to AppConfig(
            appName = "Genshin Impact",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Exploring Teyvat", "Adventure time"),
                ActivityState("In Domain", "Artifact farming"),
                ActivityState("In Spiral Abyss", "Challenge mode"),
                ActivityState("Doing Commissions", "Daily tasks"),
                ActivityState("Pulling for Characters", "Wish upon a star")
            )
        ),
        MOBILE_LEGENDS_PACKAGE to AppConfig(
            appName = "Mobile Legends",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Match", "MOBA action"),
                ActivityState("In Ranked", "Climbing ranks"),
                ActivityState("In Brawl", "Quick match"),
                ActivityState("In Lobby", "Finding match")
            )
        ),
        AMONG_US_PACKAGE to AppConfig(
            appName = "Among Us",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Being Sus", "Emergency meeting!"),
                ActivityState("Doing Tasks", "Totally not the impostor"),
                ActivityState("In Meeting", "Discussing who's sus"),
                ActivityState("Crewmate Life", "Working on tasks")
            )
        ),
        MINECRAFT_PACKAGE to AppConfig(
            appName = "Minecraft",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Mining Diamonds", "Deep underground"),
                ActivityState("Building", "Creative mode"),
                ActivityState("Surviving", "Stay alive"),
                ActivityState("Exploring", "Finding new biomes"),
                ActivityState("In Multiplayer", "Playing with friends")
            )
        ),
        ROBLOX_PACKAGE to AppConfig(
            appName = "Roblox",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("In Experience", "Playing a game"),
                ActivityState("Creating", "Building experiences"),
                ActivityState("Hanging Out", "With friends")
            )
        ),
        SUBWAY_SURFERS_PACKAGE to AppConfig(
            appName = "Subway Surfers",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Running", "Escaping the inspector"),
                ActivityState("Collecting Coins", "High score attempt"),
                ActivityState("In World Tour", "New city")
            )
        ),
        CANDY_CRUSH_PACKAGE to AppConfig(
            appName = "Candy Crush",
            defaultActivity = "Playing",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Crushing Candies", "Sweet combos"),
                ActivityState("On a Level", "Puzzle solving"),
                ActivityState("In Event", "Special challenge")
            )
        ),
        
        // ==================== UTILITY ====================
        NET_MIRROR_PACKAGE to AppConfig(
            appName = "Net Mirror",
            defaultActivity = "Mirroring",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Screen Mirroring", "Casting to display"),
                ActivityState("Streaming Content", "Sharing screen"),
                ActivityState("Presenting", "Presentation mode")
            )
        ),
        CHROME_PACKAGE to AppConfig(
            appName = "Chrome",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Browsing the Web", "Surfing the internet"),
                ActivityState("Researching", "Finding information"),
                ActivityState("Reading", "Catching up on content")
            )
        ),
        EDGE_PACKAGE to AppConfig(
            appName = "Microsoft Edge",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Browsing the Web", "Surfing the internet"),
                ActivityState("Researching", "Finding information")
            )
        ),
        FIREFOX_PACKAGE to AppConfig(
            appName = "Firefox",
            defaultActivity = "Browsing",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Browsing the Web", "Private browsing"),
                ActivityState("Researching", "Finding information")
            )
        ),
        
        // ==================== EDUCATION ====================
        PW_PACKAGE to AppConfig(
            appName = "Physics Wallah",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Attending Live Class", "Learning in real-time"),
                ActivityState("Watching Lecture", "Video learning"),
                ActivityState("Taking Notes", "Active learning"),
                ActivityState("Solving DPP", "Practice time"),
                ActivityState("Taking Test", "Assessment mode"),
                ActivityState("Browsing Courses", "Exploring subjects")
            )
        ),
        PW_PATHSHALA_PACKAGE to AppConfig(
            appName = "PW Pathshala",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Attending Class", "Learning mode"),
                ActivityState("Watching Lecture", "Video learning"),
                ActivityState("Practicing", "Problem solving")
            )
        ),
        UNACADEMY_PACKAGE to AppConfig(
            appName = "Unacademy",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("In Live Class", "Real-time learning"),
                ActivityState("Watching Course", "Self-paced study"),
                ActivityState("Taking Test", "Assessment mode"),
                ActivityState("Practicing", "Mock tests")
            )
        ),
        BYJUS_PACKAGE to AppConfig(
            appName = "BYJU'S",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Lessons", "Interactive learning"),
                ActivityState("Practicing", "Exercise mode"),
                ActivityState("Taking Test", "Assessment")
            )
        ),
        VEDANTU_PACKAGE to AppConfig(
            appName = "Vedantu",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("In Live Class", "Interactive session"),
                ActivityState("Watching Recording", "Catching up"),
                ActivityState("Doubt Solving", "Clearing concepts")
            )
        ),
        DOUBTNUT_PACKAGE to AppConfig(
            appName = "Doubtnut",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Solving Doubts", "Getting answers"),
                ActivityState("Watching Solutions", "Step-by-step learning"),
                ActivityState("Practicing", "Problem solving")
            )
        ),
        KHAN_ACADEMY_PACKAGE to AppConfig(
            appName = "Khan Academy",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Watching Lessons", "Free education"),
                ActivityState("Practicing", "Exercises"),
                ActivityState("Earning Points", "Gamified learning")
            )
        ),
        COURSERA_PACKAGE to AppConfig(
            appName = "Coursera",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Taking Course", "University content"),
                ActivityState("Watching Lectures", "Video learning"),
                ActivityState("Completing Assignments", "Project work")
            )
        ),
        UDEMY_PACKAGE to AppConfig(
            appName = "Udemy",
            defaultActivity = "Learning",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Taking Course", "Skill building"),
                ActivityState("Watching Lectures", "Video learning"),
                ActivityState("Practicing", "Hands-on learning")
            )
        ),
        
        // ==================== READING/NEWS ====================
        KINDLE_PACKAGE to AppConfig(
            appName = "Kindle",
            defaultActivity = "Reading",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading a Book", "Lost in pages"),
                ActivityState("Highlighting", "Saving notes"),
                ActivityState("Browsing Library", "Finding next read")
            )
        ),
        GOOGLE_NEWS_PACKAGE to AppConfig(
            appName = "Google News",
            defaultActivity = "Reading",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading News", "Staying informed"),
                ActivityState("Browsing Headlines", "Quick updates"),
                ActivityState("Following Topics", "Personalized news")
            )
        ),
        INSHORTS_PACKAGE to AppConfig(
            appName = "Inshorts",
            defaultActivity = "Reading",
            activityType = ACTIVITY_WATCHING,
            activities = listOf(
                ActivityState("Reading News", "60 words or less"),
                ActivityState("Swiping Stories", "Quick news")
            )
        ),
        
        // ==================== FITNESS ====================
        STRAVA_PACKAGE to AppConfig(
            appName = "Strava",
            defaultActivity = "Training",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("Recording Activity", "Tracking workout"),
                ActivityState("Running", "On a run"),
                ActivityState("Cycling", "Bike ride"),
                ActivityState("Checking Feed", "Social fitness")
            )
        ),
        NIKE_RUN_PACKAGE to AppConfig(
            appName = "Nike Run Club",
            defaultActivity = "Running",
            activityType = ACTIVITY_PLAYING,
            activities = listOf(
                ActivityState("On a Run", "Just Do It"),
                ActivityState("Guided Run", "Coach mode"),
                ActivityState("Checking Stats", "Progress tracking")
            )
        )
    )

    /**
     * Get enhanced activity information for the given package.
     * Randomly selects an activity from the configured activities for variety.
     */
    fun getEnhancedActivity(packageName: String, appName: String): CommonRpc {
        val config = appActivityConfig[packageName]

        return if (config != null) {
            // Randomly select an activity for more dynamic presence
            val activity = if (config.activities.isNotEmpty()) {
                config.activities.random()
            } else {
                ActivityState(config.defaultActivity, null)
            }

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
