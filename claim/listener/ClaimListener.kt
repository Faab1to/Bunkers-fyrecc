package cc.fyre.bunkers.claim.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.claim.data.ClaimSelection
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @project hcf
 *
 * @date 01/05/2020
 * @author xanderume@gmail.com
 */
class ClaimListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerQuit(event: PlayerQuitEvent) {

        if (!this.instance.claimHandler.findSelection(event.player.uniqueId).isPresent) {
            return
        }

        event.player.inventory.remove(ClaimSelection.ITEM)

        this.instance.claimHandler.selections.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerDropItem(event: PlayerDropItemEvent) {

        if (!event.itemDrop.itemStack.isSimilar(ClaimSelection.ITEM))  {
            return
        }

        event.itemDrop.remove()

        this.instance.claimHandler.selections.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerDamageItem(event: PlayerItemDamageEvent) {

        if (!event.item.isSimilar(ClaimSelection.ITEM)) {
            return
        }

        if (!this.instance.claimHandler.findSelection(event.player.uniqueId).isPresent) {
            return
        }

        event.isCancelled = true
    }

}