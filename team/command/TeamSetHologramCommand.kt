package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team

import cc.fyre.engine.map.data.Map
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
object TeamSetHologramCommand {

    @JvmStatic
    @Command(names = ["team sethologram","t sethologram","faction sethologram","fac sethologram","f sethologram"],async = true,permission = "bunkers.command.team.sethologram")
    fun execute(player: Player, @Param(name = "team")team: Team, @Param(name = "map",defaultValue = "current")map: Map) {
        team.holograms.add(player.location.clone())

        if (!Bunkers.instance.teamHandler.saveTeamData(team,map).wasAcknowledged()) {
            player.sendMessage("${ChatColor.RED}Failed to update team data..")
            return
        }

        player.sendMessage("${ChatColor.YELLOW}Added Hologram for ${team.getDisplayName()}${ChatColor.YELLOW}'s team on map ${ChatColor.LIGHT_PURPLE}${map.id}${ChatColor.YELLOW}.")
    }

}