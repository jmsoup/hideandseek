package kr.jmsoup.hideandseek

import co.aikar.commands.PaperCommandManager
import kr.jmsoup.hideandseek.command.HideAndSeekCommand
import kr.jmsoup.hideandseek.config.PluginConfig
import kr.jmsoup.hideandseek.listener.PlayerEventListener
import kr.jmsoup.hideandseek.manager.GameManager
import kr.jmsoup.hideandseek.network.PacketHandler
import org.bukkit.plugin.java.JavaPlugin

class HideAndSeek : JavaPlugin() {
    companion object {
        const val CHANNEL = "hideandseek:generic"
    }

    lateinit var gameManager: GameManager
        private set

    lateinit var pluginConfig: PluginConfig
        private set

    override fun onEnable() {
        saveDefaultConfig()

        pluginConfig = PluginConfig(this)
        gameManager = GameManager(this)

        val packetHandler = PacketHandler(this)

        server.messenger.registerIncomingPluginChannel(this, CHANNEL, packetHandler)
        server.messenger.registerOutgoingPluginChannel(this, CHANNEL)

        server.pluginManager.registerEvents(PlayerEventListener(this), this)

        val commandManager = PaperCommandManager(this)
        commandManager.registerCommand(HideAndSeekCommand(this))

    }
}
