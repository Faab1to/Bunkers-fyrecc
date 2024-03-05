package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 20/08/2020
 * @author xanderume@gmail.com
 */
object TeamLocationCommand {

    @JvmStatic
    @Command(names = ["tl"], permission = "")
    fun execute(player: Player,@Param(name = "player",defaultValue = "self")target: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        team.sendMessage("${ChatColor.DARK_AQUA}(Team) ${target.name}: ${ChatColor.YELLOW}[${target.location.blockX}, ${target.location.blockY}, ${target.location.blockZ}] ${ChatColor.DARK_AQUA}- ${Bunkers.instance.teamHandler.findByLocation(target.location).getDisplayName()}")
    }

}