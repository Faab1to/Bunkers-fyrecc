package cc.fyre.bunkers.repair.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.repair.menu.RepairMenu
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class RepairListener(private val instance: Bunkers):Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.clickedBlock == null) {
            return
        }

        if (event.action == Action.PHYSICAL || event.action.name.contains("AIR",true)) {
            return
        }

        if (event.clickedBlock.type != Material.ANVIL) {
            return
        }

        this.instance.server.scheduler.runTaskLater(this.instance,{RepairMenu().openMenu(event.player)},1L)
    }

}