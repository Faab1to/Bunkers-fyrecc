package cc.fyre.bunkers.game.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @project bunkers
 *
 * @date 20/01/2021
 * @author xanderume@gmail.com
 */
class VotingListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        if (event.player.world != GameEngine.instance.voteHandler.world) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        if (!GameEngine.instance.gameHandler.getState().isPastOrCurrently(GameServer.State.IN_PROGRESS)) {
            return
        }

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (team.hq == null) {
            event.player.sendMessage("${ChatColor.RED}Your team's HQ seems to be missing, please contact an administrator..")
            return
        }

        Bukkit.getScheduler().runTaskLater(this.instance,{

            val koth = this.instance.teamHandler.cache[Team.Type.KOTH]!!

            event.player.teleport(team.hq)

            val teams = StringUtils.join(this.instance.teamHandler.findPlayerTeams().filter{ filtered -> filtered.type != team.type && filtered.members.isNotEmpty()}.map{ filtered -> filtered.getDisplayName()}.toTypedArray())

            arrayListOf(
                    "",
                    " ${ChatColor.GOLD}You are apart of the ${team.getDisplayName()}${ChatColor.GOLD} team.",
                    " ${ChatColor.GOLD}Make $teams${ChatColor.GOLD} raidable",
                    " ${ChatColor.GOLD}and/or capture ${koth.getDisplayName()}${ChatColor.GOLD} to win.",
                    " ${ChatColor.GOLD}Make sure to block up all ${ChatColor.RED}${ChatColor.BOLD}${team.holograms.size} ${ChatColor.GOLD}entrances!",
                    " ${ChatColor.GOLD}Food can be found outside your base.",
                    ""
            ).forEach{message -> event.player.sendMessage(message)}

        },10L)
    }
}