package kr.jmsoup.hideandseek.listener

import kr.jmsoup.hideandseek.HideAndSeek
import kr.jmsoup.hideandseek.manager.GameState
import kr.jmsoup.hideandseek.manager.SkinCacheManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEventListener(private val plugin: HideAndSeek) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for (cachedSkinPacket in SkinCacheManager.getAllCachedChunks()) {
                player.sendPluginMessage(plugin, HideAndSeek.CHANNEL, cachedSkinPacket)
            }
        }, 20L)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        SkinCacheManager.removeSkin(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val gameManager = plugin.gameManager

        if (!gameManager.isGameRunning) return
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        val state = gameManager.currentState

        if (state is GameState.Playing) {
            if (attacker.uniqueId == gameManager.seeker && victim.uniqueId in state.alive) {
                gameManager.eliminateHider(victim)
            }
        }
    }
}