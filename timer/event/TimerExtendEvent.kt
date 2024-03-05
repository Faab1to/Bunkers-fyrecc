package cc.fyre.bunkers.timer.event

import cc.fyre.bunkers.timer.data.Timer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

/**
 * @project hcf
 *
 * @date 01/11/2020
 * @author xanderume@gmail.com
 */
class TimerExtendEvent(val timer: Timer,val uuid: UUID,val newDuration: Long) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        @JvmStatic val handlerList = HandlerList()

    }

}