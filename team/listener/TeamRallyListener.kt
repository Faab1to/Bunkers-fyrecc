package cc.fyre.bunkers.team.listener

import cc.fyre.bunkers.Bunkers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @project bunkers
 *
 * @date 22/12/2020
 * @author xanderume@gmail.com
 */
class TeamRallyListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (team.rally == null) {
            return
        }

       //TODO LunarClientAPI.instance.packetHandler.sendPacket(event.player,WayPointAddPacket("Rally",team.rally!!.world,team.rally!!.blockX,team.rally!!.blockY,team.rally!!.blockZ,Color.ORANGE,forced = true,visible = true))
    }

}