package cc.fyre.bunkers.team.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.engine.GameEngine
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * @project bunkers
 *
 * @date 19/08/2020
 * @author xanderume@gmail.com
 */
class TeamChatListener(private val instance: Bunkers):Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onAsyncChat(event: AsyncPlayerChatEvent) {

        if (event.isCancelled) {
            return
        }

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (GameEngine.instance.spectateHandler.isSpectating(event.player)) {
            return
        }

        val teamPrefix = event.message[0] == '@'
        val globalPrefix = event.message[0] == '!'

        if ((teamPrefix || globalPrefix)) {

            if (event.message.length == 1) {
                event.player.sendMessage("${ChatColor.RED}You must supply a message.")
                event.isCancelled = true
                return
            }

            event.message = event.message.substring(1).trim()
        }

        if (teamPrefix || (team.chat[event.player.uniqueId] == true && !globalPrefix)) {
            event.recipients.clear()
            event.recipients.addAll(team.findOnlineMembers())

            event.format = "${ChatColor.DARK_AQUA}(Team) ${event.player.name}:${ChatColor.YELLOW} ${event.message}"
            return
        }

        event.format = "${ChatColor.GOLD}[${team.getDisplayName()}${ChatColor.GOLD}]${ChatColor.WHITE}${event.player.name}: ${event.message}"
    }


}