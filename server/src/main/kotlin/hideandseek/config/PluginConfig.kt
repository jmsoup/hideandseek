package kr.jmsoup.hideandseek.config

import kr.jmsoup.hideandseek.HideAndSeek
import org.bukkit.Location

class PluginConfig(private val plugin: HideAndSeek) {

    fun reload() {
        plugin.reloadConfig()
    }

    val seekerSpawn: Location?
        get() = plugin.config.getLocation("seek-location")

    val hiderSpawn: Location?
        get() = plugin.config.getLocation("hide-location")

    val hideTime: Int
        get() = plugin.config.getInt("hide-time", 30)

    val gameTime: Int
        get() = plugin.config.getInt("game-time", 300)

    val hideScale: Double
        get() = plugin.config.getDouble("hide-scale", 0.3)

    fun setSeekerSpawn(location: Location) {
        plugin.config.set("seek-location", location)
        plugin.saveConfig()
    }

    fun setHiderSpawn(location: Location) {
        plugin.config.set("hide-location", location)
        plugin.saveConfig()
    }
}