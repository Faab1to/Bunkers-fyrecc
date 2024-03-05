package cc.fyre.bunkers.villager.listener

import cc.fyre.bunkers.Bunkers
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionEffectAddEvent
import org.bukkit.event.world.ChunkUnloadEvent

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
class VillagerListener(private val instance: Bunkers):Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    private fun onChunkUnLoad(event: ChunkUnloadEvent) {

        if (this.instance.villagerHandler.cache.none{event.chunk.entities.firstOrNull{entity -> entity.uniqueId == it.uniqueID} != null}) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPotionEffectAdd(event: PotionEffectAddEvent) {

        if (event.entity !is Villager) {
            return
        }

        event.isCancelled = true
    }
}