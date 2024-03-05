package cc.fyre.bunkers.timer.listener

import cc.fyre.bunkers.Bunkers
import net.hylist.handler.MovementHandler
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.timer.event.TimerCreateEvent
import cc.fyre.bunkers.timer.event.TimerExpireEvent
import cc.fyre.engine.util.FormatUtil
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * @project hcf
 *
 * @date 06/07/2020
 * @author xanderume@gmail.com
 */
class HomeListener(private val instance: Bunkers) : Listener,MovementHandler {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerCreate(event: TimerCreateEvent) {

        if (event.timer.type != TimerType.HOME) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        player.sendMessage("${ChatColor.YELLOW}Teleporting in ${ChatColor.LIGHT_PURPLE}${FormatUtil.formatIntoDetailedString(event.timer.getDuration())}${ChatColor.YELLOW}... Stay still and do not take damage.")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerExpire(event: TimerExpireEvent) {

        if (event.timer.type != TimerType.HOME) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        val team = this.instance.teamHandler.findById(player.uniqueId) ?: return

        if (team.hq == null) {
            player.sendMessage("${ChatColor.RED}Your team's HQ has not been setup, please contact an administrator.")
            return
        }

        player.world.getEntitiesByClass(EnderPearl::class.java).filter{pearl -> pearl.shooter != null && pearl.shooter is Player && (pearl.shooter as Player).uniqueId == player.uniqueId}.forEach{pearl -> pearl.remove()}

        this.instance.server.scheduler.runTask(this.instance) {

            if (team.hq!!.chunk.isLoaded) {
                team.hq!!.chunk.load()
            }

            player.teleport(team.hq)
        }

        player.sendMessage("${ChatColor.YELLOW}Warping to ${ChatColor.LIGHT_PURPLE}${team.getDisplayName()}${ChatColor.YELLOW}${ChatColor.YELLOW}'s HQ.")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.instance.timerHandler.removeTimer(event.player.uniqueId, TimerType.HOME)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onEntityDamage(event: EntityDamageEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.damage <= 0) {
            return
        }

        if (event.entity !is Player) {
            return
        }

        if (!this.instance.timerHandler.removeTimer(event.entity.uniqueId,TimerType.HOME)) {
            return
        }

        (event.entity as Player).sendMessage("${ChatColor.RED}Teleport cancelled.")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {

        if (event.isCancelled) {
            return
        }

        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }

    override fun handleUpdateRotation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

        if (!this.instance.timerHandler.removeTimer(player.uniqueId,TimerType.HOME)) {
            return
        }

        player.sendMessage("${ChatColor.RED}Teleport cancelled.")
    }

}