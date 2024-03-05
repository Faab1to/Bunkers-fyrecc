package cc.fyre.bunkers.villager.listener

import cc.fyre.bunkers.Bunkers
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent

/**
 * @project bunkers
 *
 * @date 08/08/2020
 * @author xanderume@gmail.com
 */
class VillagerPacketListener(instance: Bunkers) : PacketAdapter(instance,PacketType.Play.Server.NAMED_SOUND_EFFECT) {

    override fun onPacketSending(event: PacketEvent) {

        val name = event.packet.strings.read(0)

        if (!name.startsWith("MOB.VILLAGER",true)) {
            return
        }

        if (name.endsWith("DEATH",true) || name.endsWith("HIT",true)) {
            return
        }

        event.isCancelled = true
    }

}