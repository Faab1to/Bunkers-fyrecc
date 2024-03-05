package cc.fyre.bunkers.timer.data

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.event.TimerExpireEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * @project hcf
 *
 * @date 06/07/2020
 * @author xanderume@gmail.com
 */
open class Timer(val type: TimerType,private val duration: Long,var owner: UUID?) {

    constructor(type: TimerType,owner: UUID?):this(type,type.duration,owner)

    var task: BukkitTask? = null

    private var paused = 0L
    private var started = System.currentTimeMillis()
    private var expires = System.currentTimeMillis() + this.duration

    init {

        if (this.owner != null) {
            this.task = this.createTask(this.duration)
        }

    }

    fun getDuration(): Long {
        return this.duration
    }

    fun isPaused():Boolean {
        return this.paused != 0L
    }

    fun setPaused(value: Boolean) {

        if (value == this.isPaused()) {
            return
        }

        if (value) {

            if (this.task != null) {
                this.task!!.cancel()
            }

            this.paused = this.getRemaining()
            return
        }

        this.setRemaining(this.paused)
        this.paused = 0L
    }

    fun getRemaining():Long {

        if (this.paused != 0L) {
            return this.paused
        }

        return this.expires - System.currentTimeMillis()
    }

    fun setRemaining(duration: Long) {

        if (duration <= 0L) {
            return
        }

        this.expires = System.currentTimeMillis() + duration

        if (this.owner == null) {
            return
        }

        if (this.task != null) {
            this.task!!.cancel()
        }

        this.task = this.createTask(duration)
    }

    private fun createTask(duration: Long): BukkitTask {
        return object : BukkitRunnable() {

            override fun run() {

                if (this@Timer.owner == null) {
                    return
                }

                if (Bunkers.instance.timerHandler.cache.containsKey(this@Timer.owner!!)) {
                    Bunkers.instance.timerHandler.cache[this@Timer.owner!!]!!.remove(this@Timer)
                }

                Bunkers.instance.server.pluginManager.callEvent(TimerExpireEvent(this@Timer,this@Timer.owner!!))
            }

        }.runTaskLater(Bunkers.instance,duration / 50L)
    }
}