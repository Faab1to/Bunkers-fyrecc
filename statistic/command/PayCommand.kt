package cc.fyre.bunkers.statistic.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.statistic.StatisticHandler
import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import cc.fyre.engine.util.FormatUtil
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @project bunkers
 *
 * @date 19/08/2020
 * @author xanderume@gmail.com
 */
object PayCommand {

    @JvmStatic
    @Command(names = ["pay"],permission = "")
    fun execute(player: Player,@Param(name = "player")uuid: UUID,@Param(name = "amount")amount: Int) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        if (amount <= 0) {
            player.sendMessage("${ChatColor.RED}Amount must be positive.")
            return
        }

        if (amount > Bunkers.instance.statisticHandler.getBalance(player.uniqueId)) {
            player.sendMessage("${ChatColor.RED}You only have $${Bunkers.instance.statisticHandler.getBalance(player.uniqueId)}.")
            return
        }

        if (player.uniqueId == uuid) {
            player.sendMessage("${ChatColor.RED}You cannot pay yourself!")
            return
        }

        if (!team.isMember(uuid)) {
            player.sendMessage("${ChatColor.RED}${UUIDUtils.name(uuid)} is not on your team.")
            return
        }

        if (GameEngine.instance.gameHandler.getState() != GameServer.State.IN_PROGRESS) {
            player.sendMessage("${ChatColor.RED}Please wait for the game to start.")
            return
        }

        val time = GameEngine.instance.gameHandler.getGameTime()

        if (time <= StatisticHandler.PAY_TIME) {
            player.sendMessage("${ChatColor.RED}You can only pay teammates ${ChatColor.WHITE}${FormatUtil.formatIntoDetailedString(StatisticHandler.PAY_TIME)}${ChatColor.RED} into the game, please wait another ${ChatColor.RED}${ChatColor.BOLD}${FormatUtil.formatIntoDetailedString(TimeUnit.MINUTES.toMillis(2L) - time)}${ChatColor.RED}!")
            return
        }

        Bunkers.instance.statisticHandler.addBalance(uuid,amount)
        Bunkers.instance.statisticHandler.addBalance(player.uniqueId,-amount)

        player.sendMessage("${ChatColor.YELLOW}You have sent ${ChatColor.LIGHT_PURPLE}$${amount}${ChatColor.YELLOW} to ${ChatColor.LIGHT_PURPLE}${UUIDUtils.name(uuid)}${ChatColor.YELLOW}.")

        Bunkers.instance.server.getPlayer(uuid)?.sendMessage("${ChatColor.LIGHT_PURPLE}${player.name}${ChatColor.YELLOW} has sent you $${ChatColor.LIGHT_PURPLE}${amount}${ChatColor.YELLOW}.")
    }

}