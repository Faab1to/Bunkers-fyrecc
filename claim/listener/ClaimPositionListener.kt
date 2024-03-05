package cc.fyre.bunkers.claim.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.claim.data.Claim
import cc.fyre.bunkers.claim.data.ClaimSelection
import org.bukkit.Bukkit

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent


/**
 * @project hcf
 *
 * @date 03/06/2020
 * @author xanderume@gmail.com
 */
class ClaimPositionListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.item == null) {
            return
        }

        if (!event.item.isSimilar(ClaimSelection.ITEM)) {
            return
        }

        val optionalSelection = this.instance.claimHandler.findSelection(event.player.uniqueId)

        if (!optionalSelection.isPresent) {
            return
        }

        val selection = optionalSelection.get()

        event.isCancelled = true

        if (event.action == Action.RIGHT_CLICK_AIR) {
            event.player.itemInHand = null
            event.player.sendMessage("${ChatColor.RED}You have cancelled the claiming process.")

            this.instance.claimHandler.selections.remove(event.player.uniqueId)
            return
        }

        if (event.action == Action.LEFT_CLICK_AIR) {

            if (!event.player.isSneaking) {
                return
            }

            if (selection.first == null || selection.second == null) {
                event.player.sendMessage("${ChatColor.RED}You have not selected both corners of your claim yet!")
                return
            }

            event.player.itemInHand = null

            this.instance.claimHandler.selections.remove(event.player.uniqueId)

            selection.team.claim = Claim(selection.first!!,selection.second!!)

            Bukkit.getServer().scheduler.runTaskAsynchronously(this.instance) {

                if (!Bunkers.instance.teamHandler.saveTeamData(selection.team,selection.map).wasAcknowledged()) {
                    event.player.sendMessage("${ChatColor.RED}Failed to update team data..")
                    return@runTaskAsynchronously
                }

                event.player.sendMessage("${ChatColor.YELLOW}Updated ${selection.team.type.getDisplayName()}${ChatColor.YELLOW}'s claim on map ${ChatColor.LIGHT_PURPLE}${selection.map.id}${ChatColor.YELLOW}.")
            }
            return
        }

        if (event.clickedBlock == null) {
            return
        }

        if (event.action == Action.LEFT_CLICK_BLOCK) {
            selection.first = event.clickedBlock.location
        } else {
            selection.second = event.clickedBlock.location
        }

        selection.getFormattedMessage(event.action,event.clickedBlock.location).filterNotNull().forEach{event.player.sendMessage(it)}
    }


}