package cc.fyre.bunkers.timer.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.event.TimerCreateEvent
import cc.fyre.bunkers.timer.event.TimerExtendEvent
import cc.fyre.bunkers.timer.event.TimerRemoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @project bunkers
 *
 * @date 23/12/2020
 * @author xanderume@gmail.com
 */
class TimerListener(private val instance: Bunkers) :Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {

        val timers = this.instance.timerHandler.cache[event.player.uniqueId] ?: return

       //TODO timers.filter{it.type.icon != null}.forEach{LunarClientAPI.instance.packetHandler.sendPacket(event.player,CooldownPacket(it.type.icon!!,it.type.name,1L))}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerCreate(event: TimerCreateEvent) {

        if (event.timer.type.icon == null) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

       //TODO LunarClientAPI.instance.packetHandler.sendPacket(player,CooldownPacket(event.timer.type.icon,event.timer.type.name,event.duration))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerRemove(event: TimerRemoveEvent) {

        if (event.timer.type.icon == null) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        //TODO LunarClientAPI.instance.packetHandler.sendPacket(player,CooldownPacket(event.timer.type.icon,event.timer.type.name,1L))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerExtend(event: TimerExtendEvent) {

        if (event.timer.type.icon == null) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        //TODO LunarClientAPI.instance.packetHandler.sendPacket(player,CooldownPacket(event.timer.type.icon,event.timer.type.name,event.newDuration))
    }

}