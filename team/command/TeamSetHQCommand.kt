package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import cc.fyre.engine.map.data.Map
import org.bukkit.ChatColor

import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
object TeamSetHQCommand {

    @JvmStatic
    @Command(names = ["team sethq","t sethq","faction sethq","fac sethq","f sethq"],async = true,permission = "bunkers.command.team.sethq")
    fun execute(player: Player,@Param(name = "team")team: Team,@Param(name = "map",defaultValue = "current")map: Map) {
        team.hq = player.location

        if (!Bunkers.instance.teamHandler.saveTeamData(team,map).wasAcknowledged()) {
            player.sendMessage("${ChatColor.RED}Failed to update team data..")
            return
        }

        player.sendMessage("${ChatColor.YELLOW}Updated ${team.getDisplayName()}${ChatColor.YELLOW}'s HQ on map ${ChatColor.LIGHT_PURPLE}${map.id}${ChatColor.YELLOW}.")
    }

}