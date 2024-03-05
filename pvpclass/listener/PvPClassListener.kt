package cc.fyre.bunkers.pvpclass.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.util.FormatUtil
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PotionEffectExpireEvent
import org.bukkit.event.inventory.EquipmentSetEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @project hcf
 *
 * @date 10/09/2020
 * @author xanderume@gmail.com
 */
class PvPClassListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        this.instance.pvpClassHandler.typeToClass.values.stream().filter{it.isEquipped(event.player)}.findFirst().ifPresent{this.instance.pvpClassHandler.setPvPClass(event.player,it.type)}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.instance.pvpClassHandler.setPvPClass(event.player,null)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEquipmentSet(event: EquipmentSetEvent) {

        if (event.humanEntity !is Player) {
            return
        }

        val current = this.instance.pvpClassHandler.findById(event.humanEntity.uniqueId)

        // current one is equipped no need to check for other one
        if (current != null && current.isEquipped(event.humanEntity as Player)) {
            return
        }

        val new = this.instance.pvpClassHandler.typeToClass.values.firstOrNull{it.isEquipped(event.humanEntity as Player)}

        if (new != null) {
            this.instance.pvpClassHandler.setPvPClass(event.humanEntity as Player,new.type)
            return
        }

        this.instance.pvpClassHandler.setPvPClass(event.humanEntity as Player,null)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.action == Action.PHYSICAL) {
            return
        }

        if (event.item == null) {
            return
        }

        val pvpClass = this.instance.pvpClassHandler.findById(event.player.uniqueId) ?: return

        val consumable = pvpClass.findConsumableByItem(event.item.type) ?: return

        val cooldown = pvpClass.getCooldown(event.player,consumable)

        if (cooldown > 0L) {
            event.player.sendMessage("${ChatColor.RED}You cannot use this for another ${ChatColor.BOLD}${FormatUtil.formatIntoFancy(cooldown)}${ChatColor.RED}.")
            return
        }

        if (!consumable.onConsume(event.player)) {
            return
        }

        pvpClass.setCooldown(event.player,consumable)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onEnergyInteract(event: PlayerInteractEvent) {

        if (!event.action.name.contains("RIGHT")) {
            return
        }

        if (event.player.itemInHand == null) {
            return
        }

        val pvpClass = this.instance.pvpClassHandler.findById(event.player.uniqueId) ?: return

        if (!pvpClass.isEnergyBased()) {
            return
        }

        val effect = pvpClass.findEffectByItem(event.player.itemInHand.type) ?: return

        val cooldown = this.instance.timerHandler.findRemaining(event.player.uniqueId,TimerType.ENERGY_COOLDOWN)

        if (cooldown > 0L) {
            event.player.sendMessage("${ChatColor.RED}You cannot use this for another ${ChatColor.BOLD}${FormatUtil.formatIntoFancy(cooldown)}${ChatColor.RED}.")
            return
        }

        val energy = this.instance.pvpClassHandler.getEnergy(event.player)

        if (energy < effect.energy) {
            event.player.sendMessage("${ChatColor.RED}You do not have enough energy for this! You need ${effect.energy} energy, but you only have ${energy.toInt()}.")
            return
        }

        if (!effect.onConsume(event.player)) {
            return
        }

        if (event.player.itemInHand.amount == 1) {
            event.player.itemInHand = null
        } else {
            event.player.itemInHand.amount = event.player.itemInHand.amount - 1
        }

        this.instance.timerHandler.addTimer(event.player.uniqueId,TimerType.ENERGY_COOLDOWN)
        this.instance.pvpClassHandler.setEnergy(event.player,energy - effect.energy)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPotionExpire(event: PotionEffectExpireEvent) {

        if (event.entity !is Player) {
            return
        }

        val previous = this.instance.pvpClassHandler.effects.remove(event.entity.uniqueId,event.effect.type)

        //restore all effects that are missing for example infinite speed 2
        if (previous == null || previous.duration >= 1_000_000) {

            val pvpClass = this.instance.pvpClassHandler.findById(event.entity.uniqueId) ?: return

            this.instance.server.scheduler.runTaskLater(this.instance,{pvpClass.type.effects.filter{!event.entity.hasPotionEffect(it.type)}.forEach{event.entity.addPotionEffect(it)}},1L)
            return
        }

        event.isCancelled = true
        event.entity.addPotionEffect(previous,true)
    }

}