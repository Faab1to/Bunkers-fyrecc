package cc.fyre.bunkers.pvpclass.event.type

import cc.fyre.bunkers.pvpclass.data.item.energy.EnergyEffect
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @project hcf
 *
 * @date 11/09/2020
 * @author xanderume@gmail.com
 */
class BardEffectEvent(val player: Player,val receivers: Collection<Player>,val effect: EnergyEffect.BardEffect) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }


}