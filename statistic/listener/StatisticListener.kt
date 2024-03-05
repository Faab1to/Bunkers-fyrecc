package cc.fyre.bunkers.statistic.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.engine.GameEngine
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @project bunkers
 *
 * @date 25/12/2020
 * @author xanderume@gmail.com
 */
class StatisticListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        this.instance.statisticHandler.playTimeJoined[event.player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        this.instance.statisticHandler.recalculatePlayTime(event.player.uniqueId)
        this.instance.statisticHandler.playTimeJoined.remove(event.player.uniqueId)
    }

}