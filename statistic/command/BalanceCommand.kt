package cc.fyre.bunkers.statistic.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

/**
 * @project bunkers
 *
 * @date 03/02/2021
 * @author xanderume@gmail.com
 */
object BalanceCommand {

    @JvmStatic
    @Command(names = ["balance","bal","money"],permission = "")
    fun execute(player: Player,@Param(name = "player",defaultValue = "self")uuid: UUID) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        Bukkit.getServer().scheduler.runTaskAsynchronously(Bunkers.instance) {

            if (!team.isMember(uuid)) {
                player.sendMessage("${ChatColor.RED}${UUIDUtils.name(uuid)} is not on your team.")
                return@runTaskAsynchronously
            }

            val balance = Bunkers.instance.statisticHandler.getBalance(uuid)

            if (player.uniqueId == uuid) {
                player.sendMessage("${ChatColor.GREEN}Balance: ${ChatColor.RED}$$balance")
                return@runTaskAsynchronously
            }

            player.sendMessage("${ChatColor.GREEN}${UUIDUtils.name(uuid)} Balance: ${ChatColor.RED}$$balance")
        }

    }

}