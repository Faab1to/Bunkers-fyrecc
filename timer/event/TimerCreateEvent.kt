package cc.fyre.bunkers.timer.event

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.Timer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

/**
 * @project hcf
 *
 * @date 06/07/2020
 * @author xanderume@gmail.com
 */
class TimerCreateEvent(val timer: Timer,val uuid: UUID,val duration: Long) : Event(),Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    fun call(): TimerCreateEvent {
        Bunkers.instance.server.pluginManager.callEvent(this)
        return this
    }

}