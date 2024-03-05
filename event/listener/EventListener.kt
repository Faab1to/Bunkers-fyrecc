package cc.fyre.bunkers.event.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.GameEngine
import net.hylist.handler.MovementHandler
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class EventListener(private val instance: Bunkers):Listener,MovementHandler {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {

        if (!this.instance.eventHandler.isActive()) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        if (this.instance.eventHandler.controller == null || this.instance.eventHandler.controller != event.player.uniqueId) {
            return
        }

        this.instance.eventHandler.setController(GameEngine.instance.gameHandler.getPlayers().filter{it.uniqueId != event.player.uniqueId}.firstOrNull{this.instance.eventHandler.isInsideCaptureZone(it.location, it)})
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDeath(event: PlayerDeathEvent) {

        if (!this.instance.eventHandler.isActive()) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(event.entity)) {
            return
        }

        if (this.instance.eventHandler.controller == null || this.instance.eventHandler.controller != event.entity.uniqueId) {
            return
        }

        this.instance.eventHandler.setController(GameEngine.instance.gameHandler.getPlayers().filter{it.uniqueId != event.entity.uniqueId}.firstOrNull{this.instance.eventHandler.isInsideCaptureZone(it.location, it)})
        return
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }
    
    override fun handleUpdateRotation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

        if (!this.instance.eventHandler.isActive()) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(player)) {
            return
        }

        if (GameEngine.instance.spectateHandler.isSpectating(player)) {
            return
        }

        if (this.instance.eventHandler.controller != null) {

            if (player.uniqueId != this.instance.eventHandler.controller) {
                return
            }

            if (this.instance.eventHandler.isInsideCaptureZone(to, player)) {
                return
            }

            this.instance.eventHandler.setController(GameEngine.instance.gameHandler.getPlayers().filter{it.uniqueId != player.uniqueId}.firstOrNull{this.instance.eventHandler.isInsideCaptureZone(it.location, it)})
            return
        }

        if (!this.instance.eventHandler.isInsideCaptureZone(to, player)) {
            return
        }

        this.instance.eventHandler.setController(player)
    }

}