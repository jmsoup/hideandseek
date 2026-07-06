package kr.jmsoup.hideandseek.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import kr.jmsoup.hideandseek.HideAndSeek
import kr.jmsoup.hideandseek.network.NetworkUtils
import kr.jmsoup.hideandseek.network.PacketAction
import kr.jmsoup.hideandseek.network.PacketHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("숨바꼭질|paint")
@CommandPermission("hideandseek.admin")
class HideAndSeekCommand(private val plugin: HideAndSeek) : BaseCommand() {

    @Default
    fun onDefault(sender: CommandSender) {
        sender.sendMessage("§f/숨바꼭질 시작")
        sender.sendMessage("§f/숨바꼭질 종료")
    }

    @Subcommand("시작")
    fun onStart(sender: CommandSender) {
        val player = sender as? Player
        plugin.gameManager.startGame(player)
    }

    @Subcommand("종료")
    fun onStop(sender: CommandSender) {
        val player = sender as? Player
        plugin.gameManager.stopGame(player)
    }

    @Subcommand("설정 제외")
    fun onExclude(sender: CommandSender, target: Player) {
        plugin.gameManager.excludedPlayers.add(target.uniqueId)
        sender.sendMessage("§c${target.name}님을 게임에서 제외했습니다.")
    }

    @Subcommand("설정 추가")
    fun onInclude(sender: CommandSender, target: Player) {
        plugin.gameManager.excludedPlayers.remove(target.uniqueId)
        sender.sendMessage("§a${target.name}님을 게임에 추가했습니다.")
    }

    @Subcommand("설정 닉네임")
    fun onNickname(sender: CommandSender, boolean: Boolean, player: Player?) {
        PacketHandler.sendPacket(plugin, PacketAction.SHOW_NAME, boolean, player)
    }

    @Subcommand("설정 모드")
    fun onMode(sender: CommandSender, boolean: Boolean, player: Player?) {
        PacketHandler.sendPacket(plugin, PacketAction.SET_MODE, boolean, player)
    }

    @Subcommand("설정 위치1")
    fun setSeekerSpawn(sender: Player) {
        plugin.pluginConfig.setSeekerSpawn(sender.location)

        sender.sendMessage("§a술래 시작 위치가 설정되었습니다.")
    }

    @Subcommand("설정 위치2")
    fun setHiderSpawn(sender: Player) {
        plugin.pluginConfig.setHiderSpawn(sender.location)

        sender.sendMessage("§a숨는 사람 시작 위치가 설정되었습니다.")
    }

    @Subcommand("리로드")
    fun reload(sender: CommandSender) {
        plugin.pluginConfig.reload()

        sender.sendMessage("§a리로드 완료")
    }
}