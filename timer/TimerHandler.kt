package cc.fyre.bunkers.timer

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers

import cc.fyre.bunkers.timer.data.Timer
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.timer.event.TimerCreateEvent
import cc.fyre.bunkers.timer.event.TimerExtendEvent
import cc.fyre.bunkers.timer.event.TimerRemoveEvent
import cc.fyre.bunkers.timer.listener.*

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

/**
 * @project hcf
 *
 * @date 06/07/2020
 * @author xanderume@gmail.com
 */
class TimerHandler(private val instance: Bunkers) {

    val cache = ConcurrentHashMap<UUID,HashSet<Timer>>()

    init {
        HylistSpigot.INSTANCE.addMovementHandler(HomeListener(this.instance))
        HylistSpigot.INSTANCE.addMovementHandler(RespawnListener(this.instance))

        this.instance.server.pluginManager.registerEvents(HomeListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(TimerListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(RespawnListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(AntidoteListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(EnderpearlListener(this.instance),this.instance)
    }

    fun findTimers(uuid: UUID):HashSet<Timer> {
        return this.cache[uuid] ?: HashSet()
    }

    fun addTimer(uuid: UUID,type: TimerType) {
        this.addTimer(uuid,type,type.duration)
    }

    fun addTimer(uuid: UUID,type: TimerType,duration: Long) {

        this.cache.putIfAbsent(uuid,HashSet())

        var timer = this.cache[uuid]!!.firstOrNull{it.type == type}

        if (timer != null) {
            timer.setRemaining(duration)
            this.instance.server.pluginManager.callEvent(TimerExtendEvent(timer,uuid,duration))
            return
        }

        timer = Timer(type,duration,uuid)

        val event = TimerCreateEvent(timer,uuid,duration)

        this.instance.server.pluginManager.callEvent(event)

        if (event.isCancelled) {
            return
        }

        this.cache[uuid]!!.add(timer)
    }

    fun removeTimer(uuid: UUID,type: TimerType):Boolean {

        if (!this.cache.containsKey(uuid)) {
            return false
        }

        val timer = this.cache[uuid]!!.firstOrNull{it.type == type} ?: return false

        timer.task?.cancel()

        this.instance.server.pluginManager.callEvent(TimerRemoveEvent(timer,uuid))

        return this.cache[uuid]!!.remove(timer)
    }

    fun hasTimer(uuid: UUID,type: TimerType):Boolean {

        if (!this.cache.containsKey(uuid)) {
            return false
        }

        return this.cache[uuid]!!.any{it.type == type && it.getRemaining() > 0}
    }

    fun setPaused(uuid: UUID,type: TimerType,value: Boolean):Boolean {

        if (!this.cache.containsKey(uuid)) {
            return false
        }

        val timer = this.cache[uuid]!!.firstOrNull{it.type == type} ?: return false

        timer.setPaused(value)
        return true
    }

    fun findRemaining(uuid: UUID,type: TimerType):Long {

        if (!this.cache.containsKey(uuid)) {
            return 0L
        }

        return this.cache[uuid]!!.firstOrNull{it.type == type}?.getRemaining() ?: 0L
    }


}