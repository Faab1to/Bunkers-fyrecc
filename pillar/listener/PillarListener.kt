package cc.fyre.bunkers.pillar.listener

import cc.fyre.bunkers.Bunkers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @project hcf
 *
 * @date 28/08/2020
 * @author xanderume@gmail.com
 */
class PillarListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.instance.pillarHandler.findPillars(event.player.uniqueId).forEach{this.instance.pillarHandler.cache.remove(event.player.uniqueId,it.type)}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        this.instance.pillarHandler.findPillars(event.entity.uniqueId).forEach{this.instance.pillarHandler.cache.remove(event.entity.uniqueId,it.type)}
    }

}