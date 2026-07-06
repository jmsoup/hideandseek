package kr.jmsoup.hideandseek.manager

import kr.jmsoup.hideandseek.HideAndSeek
import kr.jmsoup.hideandseek.network.PacketAction
import kr.jmsoup.hideandseek.network.PacketHandler
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

sealed interface GameState {
    object Idle : GameState

    data class Hiding(val remainingTime: Int, val maxTime: Int, val alive: Set<UUID>) : GameState
    data class Playing(val remainingTime: Int, val maxTime: Int, val alive: Set<UUID>) : GameState
}

class GameManager(private val plugin: HideAndSeek) {
    var currentState: GameState = GameState.Idle
        private set

    var seeker: UUID? = null
        private set

    val isGameRunning: Boolean
        get() = currentState !is GameState.Idle

    private var gameTask: BukkitTask? = null
    private var bossBar: BossBar? = null
    val excludedPlayers = mutableSetOf<UUID>()

    fun startGame(sender: Player?) {
        if (isGameRunning) {
            sender?.sendMessage("§c이미 게임이 진행중입니다.")
            return
        }

        val players = Bukkit.getOnlinePlayers().filter { it.uniqueId !in excludedPlayers }.toList()
        if (players.size < 2) {
            sender?.sendMessage("§c최소 2명의 플레이어가 필요합니다.")
            return
        }

        val config = plugin.pluginConfig
        val seekerPlayer = players.random()
        seeker = seekerPlayer.uniqueId

        val hiders = players
            .filter { it.uniqueId != seeker }
            .map { it.uniqueId }
            .toSet()

        currentState = GameState.Hiding(remainingTime = config.hideTime, maxTime = config.hideTime, alive = hiders)

        PacketHandler.sendPacket(plugin, PacketAction.SHOW_NAME, false)
        PacketHandler.sendPacket(plugin, PacketAction.SET_MODE, true)

        bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_12)

        players.forEach { player ->
            bossBar?.addPlayer(player)
            val isSeeker = player.uniqueId == seeker
            player.getAttribute(Attribute.SCALE)?.baseValue = if(isSeeker) 1.0 else config.hideScale
            (if(isSeeker) config.seekerSpawn else config.hiderSpawn)?.let { player.teleport(it) }
        }

        Bukkit.broadcastMessage("§a${seekerPlayer.name}님이 술래로 선정되었습니다.")
        updateBossBar()
        runGameTimer()
    }

    fun eliminateHider(player: Player) {
        val uuid = player.uniqueId

        val newHiders = when (val state = currentState) {
            is GameState.Idle -> return
            is GameState.Hiding -> {
                if (uuid !in state.alive) return
                val updated = state.alive - uuid
                currentState = state.copy(alive = updated)
                updated
            }
            is GameState.Playing -> {
                if (uuid !in state.alive) return
                val updated = state.alive - uuid
                currentState = state.copy(alive = updated)
                updated
            }
        }

        player.gameMode = GameMode.SPECTATOR

        Bukkit.broadcastMessage("§c${player.name}님이 탈락했습니다! §7(남은 생존자: ${newHiders.size}명)")

        if (newHiders.isEmpty()) {
            Bukkit.broadcastMessage("§a술래가 승리했습니다!")
            stopGame(null)
        }
    }

    private fun runGameTimer() {
        val config = plugin.pluginConfig

        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            when (val state = currentState) {
                is GameState.Idle -> {
                    gameTask?.cancel()
                    return@Runnable
                }

                is GameState.Hiding -> {
                    val nextTime = state.remainingTime - 1
                    if (nextTime <= 0) {
                        currentState = GameState.Playing(remainingTime = config.gameTime, maxTime = config.gameTime, alive = state.alive)
                        Bukkit.broadcastMessage("§a술래가 풀려났습니다!")
                    } else {
                        currentState = state.copy(remainingTime = nextTime)
                    }
                }

                is GameState.Playing -> {
                    val nextTime = state.remainingTime - 1
                    if (nextTime <= 0) {
                        val survivors = state.alive
                            .mapNotNull { Bukkit.getPlayer(it)?.name }
                            .joinToString("§f, §a")

                        Bukkit.broadcastMessage("§a생존자들의 승리입니다!")
                        Bukkit.broadcastMessage("§e생존: §a$survivors")
                        stopGame(null)
                        return@Runnable
                    } else {
                        currentState = state.copy(remainingTime = nextTime)
                    }
                }
            }

            updateBossBar()

        }, 20L, 20L)
    }

    private fun updateBossBar() {
        val bar = bossBar ?: return

        when (val state = currentState) {
            is GameState.Hiding -> {
                bar.setTitle("§a${state.remainingTime}초 §f후 술래가 풀려납니다.")
                bar.progress = (state.remainingTime.toDouble() / state.maxTime).coerceIn(0.0, 1.0)
            }
            is GameState.Playing -> {
                bar.setTitle("§f게임 종료까지 §a${state.remainingTime}초 §f남았습니다.")
                bar.progress = (state.remainingTime.toDouble() / state.maxTime).coerceIn(0.0, 1.0)
            }
            else -> bar.removeAll()
        }
    }

    fun stopGame(sender: Player?) {
        if (!isGameRunning) {
            sender?.sendMessage("§c진행 중인 게임이 없습니다.")
            return
        }

        currentState = GameState.Idle
        seeker = null

        gameTask?.cancel()
        gameTask = null

        bossBar?.removeAll()
        bossBar = null

        PacketHandler.sendPacket(plugin, PacketAction.SHOW_NAME, true)
        PacketHandler.sendPacket(plugin, PacketAction.SET_MODE, false)

        Bukkit.getOnlinePlayers().forEach { player ->
            player.getAttribute(Attribute.SCALE)?.baseValue = 1.0
            if (player.gameMode == GameMode.SPECTATOR) {
                player.gameMode = GameMode.SURVIVAL
            }
        }

        Bukkit.broadcastMessage("§c게임이 종료되었습니다.")
    }
}