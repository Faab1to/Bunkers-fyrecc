package cc.fyre.bunkers.team.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import com.google.common.collect.ImmutableSet
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
class TeamProtectionListener(private val instance: Bunkers) : Listener {

    private val blocks = HashSet<Location>()
    private val restricted = HashMap<UUID,Long>()

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    private fun onBlockPlace(event: BlockPlaceEvent) {

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.block.location)
        
        if (team.isRaidable()) {
            return
        }

        if (team.isMember(event.player)) {

            if ((team.hq?.distance(event.block.location) ?: 10.0) <= 5.0) {
                event.player.sendMessage("${ChatColor.RED}You cannot place this close to your team's HQ!")
                event.isCancelled = true
                return
            }

            this.blocks.add(event.block.location)
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.YELLOW}You cannot build in ${team.getDisplayName()}${ChatColor.YELLOW}'s territory!")
    }

    @EventHandler(priority = EventPriority.NORMAL,ignoreCancelled = true)
    private fun onBlockBreak(event: BlockBreakEvent) {

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (this.instance.supplyHandler.getSupplyByOrigin(event.block.type) != null) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.block.location)

        if (team.isRaidable()) {
            return
        }

        if (team.isMember(event.player.uniqueId)) {

            if (this.blocks.remove(event.block.location)) {
                return
            }

            event.isCancelled = true
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.YELLOW}You cannot build in ${team.getDisplayName()}${ChatColor.YELLOW}'s territory!")

        if (team.type == Team.Type.WAR_ZONE) {
            return
        }

        if (!event.block.type.isBlock) {
            return
        }

        this.restricted[event.player.uniqueId] = System.currentTimeMillis() + 1000L
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.RIGHT_CLICK_AIR) {
            return
        }

        if (event.action == Action.PHYSICAL) {
            event.isCancelled = true
            return
        }

        if (event.clickedBlock == null) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.clickedBlock.location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }

        if (NO_INTERACT.none{it == event.clickedBlock.type}) {
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.YELLOW}You cannot do this in ${team.getDisplayName()}${ChatColor.YELLOW}'s territory!")
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onHangingPlace(event: HangingPlaceEvent) {

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.block.location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }
        
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {

        if (event.remover !is Player) {
            return
        }

        val player = event.remover as Player

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.entity.location)

        if (team.isRaidable() || team.isMember(player)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    private fun onInteractEntity(event: PlayerInteractEntityEvent) {

        if (event.rightClicked !is ItemFrame) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.rightClicked.location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    private fun onEntityDamageItemFrame(event: EntityDamageByEntityEvent) {

        if (event.entity !is ItemFrame) {
            return
        }

        if (event.damager !is Player && !(event.damager is Projectile && (event.damager as Projectile).shooter is Player)) {
            return
        }

        val player = if (event.damager is Player) event.damager as Player else (event.damager as Projectile).shooter as Player

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.entity.location)

        if (team.isRaidable() || team.isMember(player)) {
            return
        }

        event.isCancelled = true
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onBlockIgnite(event: BlockIgniteEvent) {

        if (event.player == null || event.player.gameMode != GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.block.location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }

        if (event.cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && (team.isMember(event.player))) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onBucketFill(event: PlayerBucketFillEvent) {

        val location = event.blockClicked.getRelative(event.blockFace).location

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.BLUE}You cannot build in ${team.getDisplayName()}${ChatColor.YELLOW}'s territory.")
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onBucketEmpty(event: PlayerBucketEmptyEvent) {

        val location = event.blockClicked.getRelative(event.blockFace).location

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(location)

        if (team.isRaidable() || team.isMember(event.player)) {
            return
        }

        event.isCancelled = true
        event.player.sendMessage("${ChatColor.BLUE}You cannot build in ${team.getDisplayName()}${ChatColor.YELLOW}'s territory.")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.restricted.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.NORMAL,ignoreCancelled = true)
    private fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {

        if (!this.restricted.containsKey(event.damager.uniqueId)) {
            return
        }

        if (System.currentTimeMillis() >= this.restricted[event.damager.uniqueId]!!) {
            this.restricted.remove(event.damager.uniqueId)
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (event.to != team.hq) {
            return
        }

        protection[event.player.uniqueId] = false
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onEntityDamage(event: EntityDamageEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.entity !is Player) {
            return
        }

        if (protection.containsKey(event.entity.uniqueId) && event.cause == EntityDamageEvent.DamageCause.POISON) {
            event.isCancelled = true
            return
        }

        if (protection.remove(event.entity.uniqueId) == null) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onPotionSplash(event: PotionSplashEvent) {
        event.affectedEntities.removeIf{it is Player && protection.containsKey(it.uniqueId)}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onEntityDamageByEntityProtection(event: EntityDamageByEntityEvent) {

        if (event.isCancelled) {
            return
        }

        var attacker: Player? = null

        if (event.damager is Player) {
            attacker = event.damager as Player
        } else if (event.damager is Projectile && (event.damager as Projectile).shooter is Player) {
            attacker = (event.damager as Projectile).shooter as Player
        }

        if (event.entity !is Player || attacker == null) {
            return
        }

        if (protection.containsKey(event.entity.uniqueId)) {
            event.isCancelled = true
            attacker.sendMessage("${ChatColor.RED}${(event.entity as Player).name} is currently PvP Protected!")
            return
        }

        if (!protection.containsKey(event.damager.uniqueId)) {
            return
        }

        if (protection[event.damager.uniqueId] == true) {
            protection.remove(event.damager.uniqueId)
            return
        }

        event.isCancelled = true
        (event.damager as Player).sendMessage("${ChatColor.RED}Your PvP Protection will be removed upon attacking.")
        protection[event.damager.uniqueId] = true
    }

    companion object {

        val protection: HashMap<UUID,Boolean> = HashMap()
        val NO_INTERACT: ImmutableSet<Material> = ImmutableSet.of(Material.FENCE_GATE,Material.FURNACE,Material.BURNING_FURNACE,Material.BREWING_STAND,Material.CHEST,Material.HOPPER,Material.DISPENSER,Material.WOODEN_DOOR,Material.STONE_BUTTON,Material.WOOD_BUTTON,Material.TRAPPED_CHEST,Material.TRAP_DOOR,Material.LEVER,Material.DROPPER,Material.ENCHANTMENT_TABLE,Material.BED_BLOCK,Material.ANVIL,Material.BEACON)
        val ATTACK_DISABLING_BLOCKS: ImmutableSet<Material> = ImmutableSet.of(Material.GLASS, Material.WOOD_DOOR, Material.IRON_DOOR, Material.FENCE_GATE)

    }

}