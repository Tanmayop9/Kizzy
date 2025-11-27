package kizzy.gateway

import kizzy.gateway.entities.presence.Presence
import kotlinx.coroutines.CoroutineScope

sealed interface DiscordWebSocket: CoroutineScope {
    suspend fun connect()
    suspend fun sendActivity(presence: Presence)
    fun isWebSocketConnected(): Boolean
    fun close()
    
    /**
     * Join a voice channel in a guild
     * @param guildId The ID of the guild
     * @param channelId The ID of the voice channel to join
     * @param selfMute Whether to self-mute (default: true)
     * @param selfDeaf Whether to self-deafen (default: true)
     */
    suspend fun joinVoiceChannel(
        guildId: String,
        channelId: String,
        selfMute: Boolean = true,
        selfDeaf: Boolean = true
    )
    
    /**
     * Leave the current voice channel in a guild
     * @param guildId The ID of the guild to leave voice channel from
     */
    suspend fun leaveVoiceChannel(guildId: String)
}