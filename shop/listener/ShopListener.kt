package cc.fyre.bunkers.shop.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.ShopHandler
import cc.fyre.bunkers.shop.data.ShopType
import cc.fyre.bunkers.shop.service.ShopService
import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.villager.VillagerHandler
import cc.fyre.bunkers.villager.data.kb.ZeroKBProfile
import cc.fyre.engine.GameEngine
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftVillager
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
class ShopListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDeath(event: EntityDeathEvent) {

        if (event.entity !is Villager) {
            return
        }

        if (!event.entity.hasMetadata(ShopHandler.TYPE_METADATA) || !event.entity.hasMetadata(ShopHandler.TEAM_METADATA)) {
            return
        }

        val shop = ShopType.findByName(event.entity.getMetadata(ShopHandler.TYPE_METADATA)[0].asString()) ?: return
        val type = Team.Type.findByName(event.entity.getMetadata(ShopHandler.TEAM_METADATA)[0].asString()) ?: return

        ShopService(event.entity.location.clone(),shop,type).runTaskTimer(this.instance,20L,20L)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityInteract(event: PlayerInteractEntityEvent) {

        if (event.rightClicked !is Villager) {
            return
        }

        if (!event.rightClicked.hasMetadata(ShopHandler.TYPE_METADATA) || !event.rightClicked.hasMetadata(ShopHandler.TEAM_METADATA)) {
            return
        }

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return
        val type = Team.Type.findByName(event.rightClicked.getMetadata(ShopHandler.TEAM_METADATA)[0].asString()) ?: return

        if (team.type != type) {
            return
        }

        if (this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        this.instance.server.scheduler.runTask(this.instance) {
            ShopType.findByName(event.rightClicked.getMetadata(ShopHandler.TYPE_METADATA)[0].asString())?.menu?.openMenu(event.player)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDamage(event: EntityDamageEvent) {

        if (event.entity !is Villager) {
            return
        }

        if ((event.entity as Villager).kbProfile != VillagerHandler.KB_PROFILE) {
            (event.entity as CraftVillager).kbProfile = VillagerHandler.KB_PROFILE
        }

        if (!event.entity.hasMetadata(ShopHandler.TYPE_METADATA) || !event.entity.hasMetadata(ShopHandler.TEAM_METADATA)) {
            return
        }

        if (event !is EntityDamageByEntityEvent) {
            event.isCancelled = true
            return
        }

        if (event.damager !is Player) {
            return
        }

        val team = this.instance.teamHandler.findById(event.damager.uniqueId)

        if (team == null) {
            event.isCancelled = true
            return
        }

        val type = Team.Type.findByName(event.entity.getMetadata(ShopHandler.TEAM_METADATA)[0].asString()) ?: return

        if (team.type != type) {
            event.damage = event.damage / 1.5F
            return
        }

        event.isCancelled = true
    }

}