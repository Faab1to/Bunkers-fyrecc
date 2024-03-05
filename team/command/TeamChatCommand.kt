package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 19/08/2020
 * @author xanderume@gmail.com
 */
object TeamChatCommand {

    @JvmStatic
    @Command(names = ["team chat","t chat","team c","t c"],permission = "")
    fun execute(player: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        team.chat[player.uniqueId] = !(team.chat[player.uniqueId] ?: false)

        player.sendMessage("${ChatColor.YELLOW}You are now talking in ${if (team.chat[player.uniqueId]!!) "${ChatColor.BLUE}team" else "${ChatColor.RED}public"}${ChatColor.YELLOW} chat.")
    }

    @JvmStatic
    @Command(names = ["publicchat","pc"], permission = "")
    fun publicChat(player: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        team.chat[player.uniqueId] = false

        player.sendMessage("${ChatColor.YELLOW}You are now talking in ${ChatColor.RED}public${ChatColor.YELLOW} chat.")
    }

    @JvmStatic
    @Command(names = ["teamchat","tc"], permission = "")
    fun teamChat(player: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        team.chat[player.uniqueId] = true

        player.sendMessage("${ChatColor.YELLOW}You are now talking in ${ChatColor.BLUE}team${ChatColor.YELLOW} chat.")
    }


}