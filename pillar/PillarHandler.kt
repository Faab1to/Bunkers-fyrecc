package cc.fyre.bunkers.pillar

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.pillar.data.Pillar

import cc.fyre.bunkers.pillar.data.adapter.WallAdapter

import cc.fyre.bunkers.pillar.listener.PillarListener
import cc.fyre.bunkers.pillar.listener.PillarPacketListener
import cc.fyre.bunkers.pillar.listener.PillarWallListener
import com.comphenix.protocol.ProtocolLibrary
import com.google.common.collect.HashBasedTable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Predicate
import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * @project hcf
 *
 * @date 27/08/2020
 * @author xanderume@gmail.com
 */

// Rewrite & cleanup of https://github.com/iPvP-MC/iHCF/master/com/doctordark/hcf/visualise/VisualiseHandler.java

class PillarHandler(private val instance: Bunkers) {

    val cache: HashBasedTable<UUID,Location,Pillar> = HashBasedTable.create<UUID,Location,Pillar>()
    val adapters = ArrayList<WallAdapter>()

    init {
        this.instance.server.pluginManager.registerEvents(PillarWallListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(PillarListener(this.instance),this.instance)

        ProtocolLibrary.getProtocolManager().addPacketListener(PillarPacketListener(this.instance))
    }

    fun findPillar(uuid: UUID,location: Location):Pillar? {
        return this.cache.get(uuid,location)
    }

    fun findPillars(uuid: UUID):MutableSet<Pillar> {
        return this.cache.row(uuid).values.toMutableSet()
    }

    fun findPillars(uuid: UUID,type: Pillar.Type):MutableSet<Pillar> {
        return this.cache.row(uuid).filter{it.value.type == type}.values.toMutableSet()
    }

    fun findPillarAmountByType(uuid: UUID,type: Pillar.Type):Int {

        val toReturn = HashSet<Pillar>()

        for (pillar in this.cache.row(uuid).filter{it.value.type == type}.values) {

            if (toReturn.any{it.location.blockX == pillar.location.blockX && it.location.blockZ == pillar.location.blockZ}) {
                continue
            }

            toReturn.add(pillar)
        }

        return toReturn.size
    }

    fun sendPillars(player: Player,type: Pillar.Type,locations: Collection<Location>,material: Material,overwrite: Boolean) {
        this.sendPillars(player,type,locations,material,0,overwrite)
    }

    fun sendPillars(player: Player,type: Pillar.Type,locations: Collection<Location>,material: Material,data: Int,overwrite: Boolean) {
        this.sendPillars(player,type,locations,Material.GLASS,material,data,overwrite)
    }

    fun sendPillars(player: Player,type: Pillar.Type,locations: Collection<Location>,origin: Material,material: Material,data: Int,overwrite: Boolean) {

        val pillars = HashMap<Location,Pillar>()

        for (location in locations) {

            if (this.cache.contains(player.uniqueId,location) && !overwrite) {
                continue
            }

            if (location.block.type.isSolid || location.block.type != Material.AIR) {
                continue
            }

            var blockType = origin

            if (type != Pillar.Type.CLAIM_BORDER) {

                if (location.blockY == 0 || location.blockY % 3 == 0) {
                    blockType = material
                }

            }

            val pillar = Pillar(type,location,blockType,data.toShort())

            pillars[location] = pillar

            player.sendBlockChange(location,pillar.material,pillar.data.toByte())

            this.cache.put(player.uniqueId,location,pillar)
        }

    }

    fun sendPillarsAsync(player: Player,type: Pillar.Type,locations: Collection<Location>,material: Material,overwrite: Boolean) {

        val pillars = HashMap<Location,Pillar>()

        for (location in locations) {

            if (this.cache.contains(player.uniqueId,location) && !overwrite) {
                continue
            }

            location.world.getChunkAtAsync(location) {

                if (location.block.type.isSolid || location.block.type != Material.AIR) {
                    return@getChunkAtAsync
                }

                var blockType = Material.GLASS

                if (type != Pillar.Type.CLAIM_BORDER) {

                    if (location.blockY == 0 || location.blockY % 3 == 0) {
                        blockType = material
                    }

                }

                val pillar = Pillar(type,location,blockType)

                pillars[location] = pillar

                player.sendBlockChange(location,pillar.material,pillar.data.toByte())

                this.cache.put(player.uniqueId,location,pillar)
            }

        }

    }

    fun removePillar(player: Player,location: Location) {

        val pillar = this.cache.remove(player.uniqueId,location) ?: return

        if (pillar.material == location.block.type && pillar.data == location.block.data.toShort()) {
            return
        }

        player.sendBlockChange(location,location.block.type,location.block.data)
    }

    fun removePillars(player: Player,type: Pillar.Type) {
        this.removePillars(player,type,Predicate{true})
    }

    fun removePillars(player: Player,locations: Collection<Location>) {
        locations.forEach{this.removePillar(player,it)}
    }

    fun removePillars(player: Player,type: Pillar.Type,predicate: Predicate<Pillar>) {

        if (!this.cache.containsRow(player.uniqueId)) {
            return
        }

        HashMap(this.cache.row(player.uniqueId)).filter{predicate.test(it.value) && (it.value == null || it.value.type == type)}.forEach{this.removePillar(player,it.key)}
    }

}