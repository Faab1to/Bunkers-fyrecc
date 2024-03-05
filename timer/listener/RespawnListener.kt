package cc.fyre.bunkers.timer.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.game.BunkersGameAdapter
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.bunkers.timer.event.TimerExpireEvent
import cc.fyre.engine.GameEngine
import net.hylist.handler.MovementHandler
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.spigotmc.SpigotConfig
import java.util.*
import kotlin.collections.HashMap

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class RespawnListener(private val instance: Bunkers) : Listener, MovementHandler {

    private val cache = HashMap<UUID,Location>()

    init {
        SpigotConfig.instantRespawn = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDeath(event: PlayerDeathEvent) {

        this.cache[event.entity.uniqueId] = event.entity.location

        if (!GameEngine.instance.gameHandler.isPlaying(event.entity)) {
            return
        }

        event.entity.gameMode = GameMode.CREATIVE
        event.entity.isFlying = true

        this.instance.timerHandler.addTimer(event.entity.uniqueId,TimerType.RESPAWN)
        this.instance.server.onlinePlayers.forEach{it.hidePlayer(event.entity)}

        GameEngine.instance.gameHandler.removePlayer(event.entity)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {

        if (!this.cache.containsKey(event.player.uniqueId)) {
            return
        }

        event.respawnLocation = this.cache[event.player.uniqueId]!!.clone().add(0.0,5.0,0.0)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onTimerExpire(event: TimerExpireEvent) {

        if (event.timer.type != TimerType.RESPAWN) {
            return
        }

        val player = this.instance.server.getPlayer(event.uuid) ?: return

        val team = this.instance.teamHandler.findById(player.uniqueId) ?: return

        if (team.hq == null) {
            player.sendMessage("${ChatColor.RED}Your team's HQ has not been setup, please contact an administrator.")
            return
        }

        player.gameMode = GameMode.SURVIVAL
        player.isFlying = false
        player.inventory.clear()

        this.cache.remove(player.uniqueId)

        BunkersGameAdapter.STARTER_ITEMS.forEach{player.inventory.addItem(it)}

        //need to go on main thread
        this.instance.server.scheduler.runTask(this.instance) {

            if (team.hq!!.chunk.isLoaded) {
                team.hq!!.chunk.load()
            }

            player.teleport(team.hq)
        }

        this.instance.server.onlinePlayers.forEach{it.showPlayer(player)}

        GameEngine.instance.gameHandler.addPlayer(player)

        player.sendMessage("${ChatColor.YELLOW}Warping to ${ChatColor.LIGHT_PURPLE}${team.getDisplayName()}${ChatColor.YELLOW}${ChatColor.YELLOW}'s HQ.")
    }


    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        if (!this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.player.gameMode = GameMode.CREATIVE
        event.player.isFlying = true
        event.player.inventory.clear()
        event.player.inventory.armorContents = null
        event.player.activePotionEffects.forEach{event.player.removePotionEffect(it.type)}

        this.instance.server.onlinePlayers.forEach{it.hidePlayer(event.player)}

        this.instance.timerHandler.setPaused(event.player.uniqueId,TimerType.RESPAWN,false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.instance.timerHandler.setPaused(event.player.uniqueId,TimerType.RESPAWN,true)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {

        if (event.isCancelled) {
            return
        }

        this.handleUpdateLocation(event.player,event.to,event.from,null)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onBlockBreak(event: BlockBreakEvent) {

        if (!this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onBlockPlace(event: BlockPlaceEvent) {

        if (!this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDamage(event: EntityDamageEvent) {

        if (event.entity !is Player) {
            return
        }

        if (!this.instance.timerHandler.hasTimer((event.entity as Player).uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (!this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {

        if (event.damager !is Player) {
            return
        }

        if (!this.instance.timerHandler.hasTimer(event.damager.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onInventoryClick(event: InventoryClickEvent) {

        if (event.whoClicked !is Player) {
            return
        }

        if (!this.instance.timerHandler.hasTimer((event.whoClicked as Player).uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPickupItem(event: PlayerPickupItemEvent) {

        if (!this.instance.timerHandler.hasTimer(event.player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        event.isCancelled = true
    }

    override fun handleUpdateRotation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

        if (!this.instance.timerHandler.hasTimer(player.uniqueId,TimerType.RESPAWN)) {
            return
        }

        val location = this.cache[player.uniqueId] ?: this.cache.putIfAbsent(player.uniqueId,from) ?: return

        if (location.distance(to) < 25) {
            return
        }

        player.teleport(from)
        player.sendMessage("${ChatColor.RED}You cannot move more than 25 blocks from where you died.")
    }

}