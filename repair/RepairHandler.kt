package cc.fyre.bunkers.repair

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.repair.listener.RepairListener
import org.bukkit.inventory.ItemStack
import org.spigotmc.SpigotConfig

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class RepairHandler(private val instance: Bunkers) {

    init {
        SpigotConfig.reduceArmorDamage = true

        this.instance.server.pluginManager.registerEvents(RepairListener(this.instance),this.instance)
    }

    fun calculatePrice(item: ItemStack):Int {
        return ((100.0 - (((item.type.maxDurability.toDouble() - item.durability.toDouble()) / item.type.maxDurability.toDouble()) * 100.0)) * 3.0).toInt()
    }

}