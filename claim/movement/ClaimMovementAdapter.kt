package cc.fyre.bunkers.claim.movement

import cc.fyre.bunkers.Bunkers
import net.hylist.handler.MovementHandler

import mkremins.fanciful.FancyMessage
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * @project hcf
 *
 * @date 03/06/2020
 * @author xanderume@gmail.com
 */
class ClaimMovementAdapter(private val instance: Bunkers) : MovementHandler,Listener {

    override fun handleUpdateRotation(player: Player,to: Location,from: Location,packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player,to: Location,from: Location,packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

        val toTeam = this.instance.teamHandler.findByLocation(to)
        val fromTeam = this.instance.teamHandler.findByLocation(from)

        if (fromTeam.type == toTeam.type) {
            return
        }

        FancyMessage("${ChatColor.YELLOW}Now leaving: ")
                .then(fromTeam.getDisplayName())
                .tooltip("${ChatColor.GREEN}Click to view team info.")
                .command("/team info ${fromTeam.type.name}")
                .then("${ChatColor.YELLOW} (${ChatColor.RED}Dangerous${ChatColor.YELLOW})")
                .send(player)

        FancyMessage("${ChatColor.YELLOW}Now entering: ")
                .then(toTeam.getDisplayName())
                .tooltip("${ChatColor.GREEN}Click to view team info.")
                .command("/team info ${toTeam.type.name}")
                .then("${ChatColor.YELLOW} (${ChatColor.RED}Dangerous${ChatColor.YELLOW})")
                .send(player)

    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }

}