package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import net.frozenorb.qlib.nametag.FrozenNametagHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 20/08/2020
 * @author xanderume@gmail.com
 */
object TeamFocusCommand {

    @JvmStatic
    @Command(names = ["team focus","t focus","faction focus","fac focus","f focus","focus"], permission = "")
    fun execute(player: Player,@Param(name = "player")target: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        if (team.isMember(target)) {
            player.sendMessage("${ChatColor.RED}${target.name} is on your team.")
            return
        }

        if (team.focus != null && team.focus == target.uniqueId) {
            team.focus = null
            team.sendMessage("${ChatColor.LIGHT_PURPLE}${player.name}${ChatColor.YELLOW} has un-focused ${ChatColor.LIGHT_PURPLE}${target.name}${ChatColor.YELLOW}.")
            team.findOnlineMembers().forEach{FrozenNametagHandler.reloadPlayer(it,target)}
            return
        }

        val members = team.findOnlineMembers()

        if (team.focus != null) {

            Bunkers.instance.server.getPlayer(team.focus)?.also{
                members.forEach{member -> FrozenNametagHandler.reloadPlayer(it,member)}
            }

        }

        team.focus = target.uniqueId
        team.sendMessage("${ChatColor.LIGHT_PURPLE}${player.name}${ChatColor.YELLOW} has focused ${ChatColor.LIGHT_PURPLE}${target.name}${ChatColor.YELLOW}.")

        members.forEach{FrozenNametagHandler.reloadPlayer(it,target)}
    }

}