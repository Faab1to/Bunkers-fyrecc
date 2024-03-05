package cc.fyre.bunkers.timer.event

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.Timer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

/**
 * @project hcf
 *
 * @date 19/07/2020
 * @author xanderume@gmail.com
 */
class TimerRemoveEvent(val timer: Timer, val uuid: UUID) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

    fun call(): TimerRemoveEvent {
        Bunkers.instance.server.pluginManager.callEvent(this)
        return this
    }

}