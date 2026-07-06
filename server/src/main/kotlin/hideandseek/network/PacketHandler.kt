package kr.jmsoup.hideandseek.network

import edu.umd.cs.findbugs.annotations.Nullable
import kr.jmsoup.hideandseek.HideAndSeek
import kr.jmsoup.hideandseek.manager.SkinCacheManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.ByteBuffer
import java.util.*

class PacketHandler(private val plugin: HideAndSeek) : PluginMessageListener {
    companion object {
        fun sendPacket(plugin: HideAndSeek, action: PacketAction, boolean: Boolean, player: Player? = null) {
            val data = byteArrayOf(if (boolean) 1.toByte() else 0.toByte())
            val packet = NetworkUtils.createPacket(action.id, data)

            if(player != null){
                player.sendPluginMessage(plugin, HideAndSeek.CHANNEL, packet)
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.sendPluginMessage(plugin, HideAndSeek.CHANNEL, packet)
                }
            }
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != HideAndSeek.CHANNEL) return

        try {
            val buffer = ByteBuffer.wrap(message)
            val action = PacketAction.fromId(NetworkUtils.readString(buffer))

            when (action) {
                PacketAction.UPDATE_SKIN -> handleUpdateSkin(player, message, buffer)
                else -> {

                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("패킷 디코딩 중 오류 발생: ${e.message}")
        }
    }

    private fun handleUpdateSkin(player: Player, message: ByteArray, buffer: ByteBuffer) {
        val dataLength = NetworkUtils.readVarInt(buffer)
        val dataBytes = ByteArray(dataLength)
        buffer.get(dataBytes)

        val dataBuffer = ByteBuffer.wrap(dataBytes)
        val ownerUuid = UUID(dataBuffer.long, dataBuffer.long)
        val totalChunks = dataBuffer.int
        val chunkIndex = dataBuffer.int

        SkinCacheManager.saveChunk(ownerUuid, chunkIndex, message)

        Bukkit.getOnlinePlayers()
            .filter { it.uniqueId != player.uniqueId }
            .forEach { target ->
                target.sendPluginMessage(plugin, HideAndSeek.CHANNEL, message)
            }
    }
}