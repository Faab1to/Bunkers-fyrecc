package cc.fyre.bunkers.game.listener

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import net.hylist.handler.MovementHandler
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * @project bunkers
 *
 * @date 17/12/2020
 * @author xanderume@gmail.com
 */
class LunarClientListener(private val instance: Bunkers) : Listener,MovementHandler {

    init {
        HylistSpigot.INSTANCE.addMovementHandler(this)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        this.instance.server.scheduler.runTaskLater(this.instance,{

            if (event.player == null || !event.player.isOnline) {
                return@runTaskLater
            }

            val koth = this.instance.teamHandler.cache[Team.Type.KOTH]

            if (koth?.hq != null) {
                //TODO LunarClientAPI.instance.packetHandler.sendPacket(event.player,WayPointAddPacket("${koth.getDisplayName()}${ChatColor.WHITE}",koth.hq!!.world,koth.hq!!.blockX,koth.hq!!.blockY,koth.hq!!.blockZ,koth.type.getRGBColor(),forced = true,visible = true))
            }

            val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return@runTaskLater

            if (team.hq == null) {
                return@runTaskLater
            }

            //TODO LunarClientAPI.instance.packetHandler.sendPacket(event.player,WayPointAddPacket("HQ",team.hq!!.world,team.hq!!.blockX,team.hq!!.blockY,team.hq!!.blockZ,team.type.getRGBColor(),forced = true,visible = true))
        },20L)

    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }

    override fun handleUpdateLocation(player: Player,to: Location,from: Location,packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

    }

    override fun handleUpdateRotation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {}

}