package cc.fyre.bunkers.pvpclass.data.item

import cc.fyre.bunkers.Bunkers

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

/**
 * @project hcf
 *
 * @date 10/09/2020
 * @author xanderume@gmail.com
 */
abstract class ConsumableItem(val material: Material,val cooldown: Long) {

    abstract fun onConsume(player: Player): Boolean

    class EffectConsumableItem(material: Material,cooldown: Long,private val effect: PotionEffect) : ConsumableItem(material,cooldown) {

        override fun onConsume(player: Player):Boolean {
            Bunkers.instance.pvpClassHandler.addPotionEffect(player,this.effect)
            return true
        }

    }

}