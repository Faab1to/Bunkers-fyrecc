package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import kotlin.math.min

/**
 * @project bunkers
 *
 * @date 23/12/2020
 * @author xanderume@gmail.com
 */
object
TeamSetDTRCommand {

    @JvmStatic
    @Command(names = ["team setdtr","t setdtr","faction setdtr","fac setdtr","f setdtr","setdtr"],hidden = true,permission = "bunkers.command.team.setdtr")
    fun execute(sender: CommandSender, @Param(name = "team")team: Team, @Param(name = "dtr")dtr: Double) {
        team.dtr = min(dtr,TeamHandler.PLAYERS_PER_TEAM + 1.0)

        sender.sendMessage("${ChatColor.YELLOW}You have set ${team.getDisplayName()}${ChatColor.YELLOW}'s DTR to: ${ChatColor.LIGHT_PURPLE}${team.getDTR()}")
    }

    
}