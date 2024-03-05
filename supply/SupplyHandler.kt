package cc.fyre.bunkers.supply

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.supply.data.Supply

import cc.fyre.bunkers.supply.listener.SupplyListener
import com.google.common.collect.HashBasedTable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.collections.HashSet

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class SupplyHandler(private val instance: Bunkers) {

    val cache = HashBasedTable.create<Location,Supply,BukkitTask>()

    private val supplies = HashSet<Supply>()

    init {
        this.supplies.add(Supply(Material.CROPS,ItemStack(Material.COOKIE),Material.AIR,2500L))
        this.supplies.add(Supply(Material.POTATO,ItemStack(Material.BAKED_POTATO),Material.AIR,2500L))
        this.supplies.add(Supply(Material.CARROT,ItemStack(Material.GOLDEN_CARROT),Material.AIR,2500L))

        this.supplies.add(Supply(Material.COAL_ORE,ItemStack(Material.COAL),Material.COBBLESTONE,10_000L))
        this.supplies.add(Supply(Material.IRON_ORE,ItemStack(Material.IRON_INGOT),Material.COBBLESTONE,10_000L))
        this.supplies.add(Supply(Material.GOLD_ORE,ItemStack(Material.GOLD_INGOT),Material.COBBLESTONE,20_000L))
        this.supplies.add(Supply(Material.DIAMOND_ORE,ItemStack(Material.DIAMOND),Material.COBBLESTONE,20_000L))
        this.supplies.add(Supply(Material.EMERALD_ORE,ItemStack(Material.EMERALD),Material.COBBLESTONE,30_000L))

        this.instance.server.pluginManager.registerEvents(SupplyListener(this.instance),this.instance)
    }

    fun dispose() {
        this.cache.values().forEach{it.cancel()}
    }

    fun isBeingSupplied(location: Location):Boolean {
        return this.cache.containsRow(location)
    }

    fun getSupplyByOrigin(origin: Material):Supply? {
        return this.supplies.firstOrNull{it.origin == origin}
    }

    fun addSupplyToService(block: Block,entry: Supply) {

        val task = object : BukkitRunnable() {

            override fun run() {
                this@SupplyHandler.cache.remove(block.location,entry)
                block.type = entry.origin
            }

        }.runTaskLater(this.instance,entry.duration / 50)

        this.cache.put(block.location,entry,task)
    }

}