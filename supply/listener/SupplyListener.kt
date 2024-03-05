package cc.fyre.bunkers.supply.listener

import cc.fyre.bunkers.Bunkers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class SupplyListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockBreak(event: BlockBreakEvent) {

        if (event.isCancelled) {
            return
        }

        if (this.instance.supplyHandler.isBeingSupplied(event.block.location)) {
            event.isCancelled = true
            return
        }

        val supply = this.instance.supplyHandler.getSupplyByOrigin(event.block.type) ?: return

        this.instance.statisticHandler.addOresMined(event.player.uniqueId,event.block.type)

        event.block.type = supply.replace

        event.isCancelled = true
        event.player.inventory.addItem(supply.reward)

        this.instance.supplyHandler.addSupplyToService(event.block,supply)
    }

}