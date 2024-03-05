package cc.fyre.bunkers.pillar.listener

import cc.fyre.bunkers.Bunkers
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent

import org.bukkit.GameMode
import org.bukkit.Location

/**
 * @project hcf
 *
 * @date 28/08/2020
 * @author xanderume@gmail.com
 */
class PillarPacketListener(private val instance: Bunkers) : PacketAdapter(instance,listOf(PacketType.Play.Client.BLOCK_DIG,PacketType.Play.Client.BLOCK_PLACE)) {

    override fun onPacketReceiving(event: PacketEvent) {

        if (event.packet.type == PacketType.Play.Client.BLOCK_PLACE) {

            val pillar = this.instance.pillarHandler.findPillar(event.player.uniqueId,Location(event.player.world,event.packet.integers.read(0).toDouble(),event.packet.integers.read(1).toDouble(),event.packet.integers.read(2).toDouble()))

            if (pillar == null || event.player.gameMode == GameMode.CREATIVE) {
                return
            }

            event.isCancelled = true
            return
        }

        val action = event.packet.integers.read(4)

        if (!(action == 0 || action == 2)) {
            return
        }

        val x = event.packet.integers.read(0)
        val y = event.packet.integers.read(1)
        val z = event.packet.integers.read(2)

        val location = Location(event.player.world,x.toDouble(),y.toDouble(),z.toDouble())

        val pillar = this.instance.pillarHandler.findPillar(event.player.uniqueId,location) ?: return

        event.isCancelled = true

        if (action == 2) {
            event.player.sendBlockChange(location,pillar.material,pillar.data.toByte())
            return
        }

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        event.player.sendBlockChange(location,pillar.material,pillar.data.toByte())
    }

}