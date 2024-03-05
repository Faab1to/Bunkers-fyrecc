package cc.fyre.bunkers.team.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.menu.TeamChestPurchaseMenu
import cc.fyre.bunkers.team.menu.TeamChestModifyMenu
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class TeamChestListener(private val instance: Bunkers) : Listener {

    private val placed = hashSetOf<Location>()

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockPlace(event: BlockPlaceEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.block.state !is Chest) {
            return
        }

        this.placed.add(event.block.location)

    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockBreak(event: BlockBreakEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.block.state !is Chest) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.block.location)

        if (team.findChestByBlock(event.block) != null) {
            event.isCancelled = true
            return
        }

        if (this.placed.remove(event.block.location)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.isCancelled) {
            return
        }

        val state = event.clickedBlock.state

        if (state !is Chest) {
            return
        }

        if (this.placed.contains(event.clickedBlock.location)) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.clickedBlock.location)

        if (!team.isMember(event.player.uniqueId)) {
            return
        }

        val chest = team.findChestByBlock(event.clickedBlock)

        if (chest == null) {

            if (state.inventory.holder is DoubleChest) {
                TeamChestPurchaseMenu(true,(state.inventory.holder as DoubleChest).location).openMenu(event.player)
            } else {
                TeamChestPurchaseMenu(false,state.location).openMenu(event.player)
            }

            event.isCancelled = true
            return
        }

        if (!chest.isMember(event.player.uniqueId)) {
            event.player.sendMessage("${ChatColor.RED}You cannot access this chest.")
            event.isCancelled = true
            return
        }

        if (event.action != Action.LEFT_CLICK_BLOCK) {
            return
        }

        if (chest.owner != event.player.uniqueId) {
            return
        }

        event.isCancelled = true

        TeamChestModifyMenu(chest).openMenu(event.player)
    }

}