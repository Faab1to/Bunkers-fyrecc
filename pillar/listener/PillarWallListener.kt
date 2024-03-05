package cc.fyre.bunkers.pillar.listener

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.pillar.data.Pillar
import net.hylist.handler.MovementHandler

import cc.fyre.bunkers.pillar.data.adapter.WallAdapter
import cc.fyre.bunkers.team.data.Team

import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*
import java.util.function.Predicate
import kotlin.collections.HashMap
import kotlin.math.abs


/**
 * @project hcf
 *
 * @date 16/09/2020
 * @author xanderume@gmail.com
 */
class PillarWallListener(private val instance: Bunkers) : Listener,MovementHandler {

    init {
        HylistSpigot.INSTANCE.addMovementHandler(this)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }

    override fun handleUpdateRotation(player: Player,to: Location,from: Location,packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player,to: Location,from: Location,packet: PacketPlayInFlying?) {

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) {
            return
        }

        val team = this.instance.teamHandler.findById(player.uniqueId) ?: return

        val adapters = this.instance.pillarHandler.adapters.filter{it.shouldCheck(player,team)}

        if (adapters.isEmpty()) {
            return
        }

        this.instance.pillarHandler.removePillars(player,Pillar.Type.CLAIM_BORDER,Predicate{
            return@Predicate it.location.world == to.world &&
                    abs(to.blockX - it.location.blockX) > WALL_BORDER_HORIZONTAL_DISTANCE ||
                    abs(to.blockY - it.location.blockY) > WALL_BORDER_HEIGHT_ABOVE_DIFF ||
                    abs(to.blockZ - it.location.blockZ) > WALL_BORDER_HORIZONTAL_DISTANCE
        })

        val minX = to.blockX - WALL_BORDER_HORIZONTAL_DISTANCE
        val maxX = to.blockX + WALL_BORDER_HORIZONTAL_DISTANCE
        val minZ = to.blockZ - WALL_BORDER_HORIZONTAL_DISTANCE
        val maxZ = to.blockZ + WALL_BORDER_HORIZONTAL_DISTANCE

        val minHeight: Int = to.blockY - WALL_BORDER_HEIGHT_BELOW_DIFF
        val maxHeight: Int = to.blockY + WALL_BORDER_HEIGHT_ABOVE_DIFF

        val entries = HashMap<Team,WallAdapter>()

        for (x in minX until maxX) {

            for (z in minZ until maxZ) {

                val enemy = this.instance.teamHandler.findByLocation(Location(to.world,x.toDouble(),0.0,z.toDouble()))

                for (adapter in adapters) {

                    if (!adapter.shouldApply(player,enemy,team)) {
                        continue
                    }

                    if (entries.containsKey(enemy)) {
                        // prevent flicker
                        continue
                    }

                    entries[enemy] = adapter
                }

            }

        }

        for (entry in entries) {

            if (entry.key.claim == null) {
                continue
            }

            val material = entry.value.getMaterial(player)

            if (entry.key.claim!!.contains(player.location)) {
                continue
            }

            val locations = ArrayList<Location>()

            entry.key.claim!!.borderIterator().forEachRemaining{

                if (abs(it.first - to.blockX) > WALL_BORDER_HORIZONTAL_DISTANCE) {
                    return@forEachRemaining
                }

                if (abs(it.second - to.blockZ) > WALL_BORDER_HORIZONTAL_DISTANCE) {
                    return@forEachRemaining
                }

                for (i in minHeight until maxHeight) {
                    locations.add(Location(to.world,it.first.toDouble(),i.toDouble(),it.second.toDouble()))
                }

            }

            this.instance.pillarHandler.sendPillars(player,Pillar.Type.CLAIM_BORDER,locations,material.first,material.first,material.second,true)
        }

    }

    companion object {

        private const val WALL_BORDER_HEIGHT_BELOW_DIFF = 3
        private const val WALL_BORDER_HEIGHT_ABOVE_DIFF = 4
        private const val WALL_BORDER_HORIZONTAL_DISTANCE = 7

    }

}