package cc.fyre.bunkers.villager.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.statistic.StatisticHandler
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.villager.VillagerHandler

import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import net.frozenorb.qlib.util.UUIDUtils

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
class VillagerLoggerListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onPlayerQuit(event: PlayerQuitEvent) {

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        if (GameEngine.instance.gameHandler.getState() != GameServer.State.IN_PROGRESS) {
            return
        }

        this.instance.villagerHandler.spawnCombatLogger(event.player.location,event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDeath(event: EntityDeathEvent) {

        if (event.entity !is Villager) {
            return
        }

        if (!event.entity.hasMetadata(VillagerHandler.LOGGER_OWNER_METADATA) || !event.entity.hasMetadata(VillagerHandler.LOGGER_ITEMS_METADATA)) {
            return
        }

        val uuid = UUID.fromString(event.entity.getMetadata(VillagerHandler.LOGGER_OWNER_METADATA)[0].asString()) ?: return


        val items = this.instance.villagerHandler.items.remove(uuid)

        if (items != null) {
            event.drops.clear()
            event.drops.addAll(items)
        }

        this.instance.villagerHandler.loggers.remove(uuid)

        val team = this.instance.teamHandler.findById(uuid) ?: return
        val name = UUIDUtils.name(uuid)

        if (event.entity.killer != null) {

            val balance = this.instance.statisticHandler.getBalance(event.entity.uniqueId)

            this.instance.statisticHandler.addKills(event.entity.killer.uniqueId,1)
            this.instance.statisticHandler.addBalance(event.entity.killer.uniqueId,if (balance >= StatisticHandler.BALANCE_PER_KILL) StatisticHandler.BALANCE_PER_KILL else balance)

            event.entity.killer.sendMessage("${ChatColor.GOLD}You earned ${ChatColor.WHITE}$${if (balance >= StatisticHandler.BALANCE_PER_KILL) StatisticHandler.BALANCE_PER_KILL else balance}.0${ChatColor.GOLD} for killing ${team.type.color}$name${ChatColor.GOLD}!")
        }

        team.dtr -= 1.0
        team.sendMessage("${ChatColor.RED}Member Death: ${ChatColor.WHITE}$name","${ChatColor.RED}DTR: ${ChatColor.WHITE}${team.getDTR()}")

        event.entity.world.strikeLightningEffect(event.entity.location)
        event.drops.removeIf{it.hasItemMeta() && it.itemMeta.hasLore() && ChatColor.stripColor(it.itemMeta.lore[0]).equals("Soulbound",true)}

        if (!team.isRaidable()) {
            this.instance.timerHandler.addTimer(uuid,TimerType.RESPAWN)
            this.instance.timerHandler.setPaused(uuid,TimerType.RESPAWN,true)
        }

        this.instance.server.broadcastMessage("${team.getColor()}$name${ChatColor.GOLD}[${this.instance.statisticHandler.getKills(uuid)}] ${ChatColor.YELLOW}${if (event.entity.killer == null) "died" else "was slain by ${this.instance.teamHandler.findById(event.entity.killer.uniqueId)?.getColor() ?: ChatColor.WHITE}${event.entity.killer.name}${ChatColor.GOLD}[${this.instance.statisticHandler.getKills(event.entity.killer.uniqueId)}]"}${ChatColor.YELLOW}.")

        this.instance.statisticHandler.addDeaths(uuid,1)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDamage(event: EntityDamageByEntityEvent) {

        if (event.entity !is Villager) {
            return
        }

        if (!event.entity.hasMetadata(VillagerHandler.LOGGER_OWNER_METADATA)) {
            return
        }

        val uuid = UUID.fromString(event.entity.getMetadata(VillagerHandler.LOGGER_OWNER_METADATA)[0].asString()) ?: return

        if (event.damager !is Player) {
            return
        }

        val team = this.instance.teamHandler.findById(event.damager.uniqueId) ?: return

        if (!team.isMember(uuid)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        val villager = this.instance.villagerHandler.loggers.remove(event.player.uniqueId) ?: return

        villager.bukkitEntity.remove()

        this.instance.villagerHandler.services.remove(event.player.uniqueId)
    }

}