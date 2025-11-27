package kizzy.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VoiceState payload for joining/leaving voice channels
 * Used with OpCode.VOICE_STATE (4)
 */
@Serializable
data class VoiceState(
    @SerialName("guild_id")
    val guildId: String?,
    @SerialName("channel_id")
    val channelId: String?,
    @SerialName("self_mute")
    val selfMute: Boolean = true,
    @SerialName("self_deaf")
    val selfDeaf: Boolean = true
)
