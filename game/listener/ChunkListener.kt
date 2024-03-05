package cc.fyre.bunkers.game.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.engine.map.event.MapLoadEvent
import org.bukkit.Chunk
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.scheduler.BukkitRunnable


/**
 * @project bunkers
 *
 * @date 19/01/2021
 * @author xanderume@gmail.com
 */
class ChunkListener(private val instance: Bunkers) : Listener {

    private val cache = ArrayList<Chunk>()

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkLoad(event: ChunkLoadEvent) {

        object : BukkitRunnable() {

            override fun run() {
                event.chunk.entities.filterIsInstance<Monster>().forEach{it.remove()}
            }

        }.runTaskLater(this.instance,20L)

    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onChunkUnLoad(event: ChunkUnloadEvent) {

        if (!this.cache.contains(event.chunk)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onMapLoad(event: MapLoadEvent) {

        object : BukkitRunnable() {

            override fun run() {

                for (team in this@ChunkListener.instance.teamHandler.findPlayerTeams()) {

                    val location = team.hq

                    if (location == null) {
                        this@ChunkListener.instance.server.logger.info("Failed to load chunks for ${team.getName()}, no hq set.")
                        return
                    }

                    for (x in -4 until 4) {

                        for (z in -4 until 4) {

                            location.world.getChunkAtAsync(location.blockX + (x shr 4),location.blockZ + (z shr 4)) {

                                if (this@ChunkListener.cache.contains(it)) {
                                    return@getChunkAtAsync
                                }

                                it.load()

                                this@ChunkListener.cache.add(it)
                                this@ChunkListener.instance.logger.info("Loaded chunk at ${it.x}, ${it.z} for ${team.getName()}.")
                            }
                        }
                    }

                }
            }

        }.runTaskLater(this.instance,5L)
    }

}