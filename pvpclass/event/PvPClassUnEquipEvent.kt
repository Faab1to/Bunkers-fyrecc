package cc.fyre.bunkers.pvpclass.event

import cc.fyre.bunkers.pvpclass.data.PvPClass
import org.bukkit.entity.Player

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @project hcf
 *
 * @date 11/09/2020
 * @author xanderume@gmail.com
 */
class PvPClassUnEquipEvent(val player: Player,val pvpClass: PvPClass) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

}