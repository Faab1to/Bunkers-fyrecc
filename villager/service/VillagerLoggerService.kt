package cc.fyre.bunkers.villager.service

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.villager.data.VillagerEntity
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @project bunkers
 *
 * @date 19/08/2020
 * @author xanderume@gmail.com
 */
class VillagerLoggerService(private val uuid: UUID,private val villager: VillagerEntity):BukkitRunnable() {

    private val name = this.villager.customName
    private val remaining = AtomicInteger(25)

    override fun run() {

        if (this.remaining.get() == 0 || this.villager.bukkitEntity.isDead) {
            this.cancel()

            if (!this.villager.bukkitEntity.isDead) {
                this.villager.bukkitEntity.remove()
            }

            Bunkers.instance.villagerHandler.loggers.remove(this.uuid)
            Bunkers.instance.villagerHandler.services.remove(this.uuid)
            return
        }

        this.villager.customName = "${this.name}${ChatColor.YELLOW} (${this.remaining.decrementAndGet()}s)"
    }

}