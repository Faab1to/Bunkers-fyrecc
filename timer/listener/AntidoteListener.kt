package cc.fyre.bunkers.timer.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.game.BunkersGameAdapter
import cc.fyre.bunkers.pvpclass.data.item.energy.EnergyEffect
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.timer.event.TimerCreateEvent
import cc.fyre.engine.util.FormatUtil
import net.frozenorb.qlib.util.TimeUtils
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionEffectAddEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project bunkers
 *
 * @date 03/02/2021
 * @author xanderume@gmail.com
 */
class AntidoteListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerCreate(event: TimerCreateEvent) {

        if (event.timer.type != TimerType.ANTIDOTE) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        player.sendMessage("${ChatColor.RED}You are now immune to debuffs for ${TimeUtils.formatIntoDetailedString((event.timer.getDuration() / 1000L).toInt())}.")

        object : BukkitRunnable() {

            override fun run() {

                for (effect in player.activePotionEffects) {

                    if (EnergyEffect.BardEffect.isDebuff(effect.type)) {
                        player.removePotionEffect(effect.type)
                    }

                }

            }

        }.runTaskLater(this.instance,2L)

    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreConsume(event: PlayerItemConsumeEvent) {

        if (event.item.type != Material.POTION) {
            return
        }

        if (!BunkersGameAdapter.ANTIDOTE.isSimilar(event.item)) {
            return
        }

        val cooldown = this.instance.timerHandler.findRemaining(event.player.uniqueId,TimerType.ANTIDOTE)

        if (cooldown <= 0L) {
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.RED}You cannot use this for another ${ChatColor.BOLD}${FormatUtil.formatIntoFancy(cooldown)}${ChatColor.RED}.")
        event.player.updateInventory()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onItemConsume(event: PlayerItemConsumeEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.item.type != Material.POTION) {
            return
        }

        if (!BunkersGameAdapter.ANTIDOTE.isSimilar(event.item)) {
            return
        }

        this.instance.timerHandler.addTimer(event.player.uniqueId,TimerType.ANTIDOTE)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onPotionEffectAdd(event: PotionEffectAddEvent) {

        if (event.entity !is Player) {
            return
        }

        if (!this.instance.timerHandler.hasTimer(event.entity.uniqueId,TimerType.ANTIDOTE)) {
            return
        }

        if (!EnergyEffect.BardEffect.isDebuff(event.effect.type)) {
            return
        }

        event.isCancelled = true
    }

}