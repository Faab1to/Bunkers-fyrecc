package cc.fyre.bunkers.supply.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class Supply(val origin: Material,val reward: ItemStack,val replace: Material,val duration: Long) {

    fun isFarm() = false

}