package cc.fyre.bunkers.team.listener

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.statistic.StatisticHandler
import cc.fyre.engine.GameEngine
import cc.fyre.engine.map.event.MapLoadEvent
import cc.fyre.engine.server.data.GameServer
import net.frozenorb.qlib.hologram.FrozenHologramHandler
import net.frozenorb.qlib.hologram.construct.Hologram
import net.hylist.handler.MovementHandler
import net.minecraft.server.v1_7_R4.PacketPlayInFlying
import org.bukkit.Bukkit

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPearlRefundEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project bunkers
 *
 * @date 06/08/2020
 * @author xanderume@gmail.com
 */
class TeamListener(private val instance: Bunkers) : Listener,MovementHandler {

    val cache = HashSet<Hologram>()

    init {
        HylistSpigot.INSTANCE.addMovementHandler(this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onMapLoad(event: MapLoadEvent) {
        Bukkit.getServer().scheduler.runTaskAsynchronously(this.instance) {
            this.instance.teamHandler.loadTeamDataSync(event.map)
            this.instance.teamHandler.findPlayerTeams().flatMap{it.holograms}.withIndex().forEach{

                val hologram = FrozenHologramHandler.createHologram()
                    .at(it.value.clone().add(0.0,1.5,0.0))
                    .build()

                hologram.addLines("${ChatColor.RED}${ChatColor.BOLD}WARNING!","${ChatColor.YELLOW}Blockup this entrance!")

                FrozenHologramHandler.getCache().add(hologram)

                this.cache.add(hologram)
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerDeath(event: PlayerDeathEvent) {

        val team = this.instance.teamHandler.findById(event.entity.uniqueId) ?: return

        if (event.entity.killer != null) {

            val balance = this.instance.statisticHandler.getBalance(event.entity.uniqueId)

            this.instance.statisticHandler.addKills(event.entity.killer.uniqueId,1)
            this.instance.statisticHandler.addBalance(event.entity.killer.uniqueId,if (balance >= StatisticHandler.BALANCE_PER_KILL) StatisticHandler.BALANCE_PER_KILL else balance)

            event.entity.killer.sendMessage("${ChatColor.GOLD}You earned ${ChatColor.WHITE}$${if (balance >= StatisticHandler.BALANCE_PER_KILL) StatisticHandler.BALANCE_PER_KILL else balance}.0${ChatColor.GOLD} for killing ${team.type.color}${event.entity.name}${ChatColor.GOLD}!")
        }

        team.dtr -= 1.0
        team.sendMessage(
                "${ChatColor.RED}Member Death: ${ChatColor.WHITE}${event.entity.name}",
                "${ChatColor.RED}DTR: ${ChatColor.WHITE}${team.getDTR()}"
        )

        if (team.dtr <= 0) {
            team.findOnlineMembers().forEach{TeamProtectionListener.protection.remove(it.uniqueId)}
        }

        event.entity.world.strikeLightningEffect(event.entity.location)
        event.drops.removeIf{it.hasItemMeta() && it.itemMeta.hasLore() && ChatColor.stripColor(it.itemMeta.lore[0]).equals("Soulbound",true)}

        if (team.isRaidable()) {
            GameEngine.instance.gameHandler.removePlayer(event.entity)
        }

        this.instance.statisticHandler.addDeaths(event.entity.uniqueId,1)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (!team.isRaidable()) {
            return
        }

        GameEngine.instance.spectateHandler.addSpectator(event.player)

        this.instance.statisticHandler.recalculatePlayTime(event.player.uniqueId)
        this.instance.statisticHandler.playTimeJoined.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {

        if (event.cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return
        }

        val team = this.instance.teamHandler.findByLocation(event.to)

        if (team.type.isSystem()) {
            return
        }

        if (team.isRaidable()) {
            return
        }

        if (team.isMember(event.player)) {
            return
        }

        if ((team.hq?.distance(event.to) ?: 8.0) > 7.0 || (team.hq?.distance(event.from) ?: 8.0) <= 7.0) {
            return
        }

        event.player.sendMessage("${ChatColor.RED}You cannot enderpearl this close to ${team.getDisplayName()}${ChatColor.RED}'s HQ!")
        event.isCancelled = true

        this.instance.server.pluginManager.callEvent(PlayerPearlRefundEvent(event.player))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {

        if (GameEngine.instance.gameHandler.getState().isBeforeOrCurrently(GameServer.State.COUNTDOWN)) {
            return
        }

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        object : BukkitRunnable() {

            override fun run() {

                if (!event.player.isOnline) {
                    return
                }

                team.sendInfo(event.player)
            }

        }.runTaskLater(this.instance,20L)

    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {

        if (event.entity !is Player) {
            return
        }

        val team = this.instance.teamHandler.findById(event.entity.uniqueId)

        if (team == null) {
            event.isCancelled = true
            return
        }

        val attacker = if (event.damager is Player) event.damager as Player else if (event.damager is Projectile && (event.damager as Projectile).shooter is Player) (event.damager as Projectile).shooter as Player else null ?: return

        if (attacker.uniqueId == event.entity.uniqueId) {
            return
        }

        if (!team.isMember(attacker.uniqueId)) {
            return
        }

        event.isCancelled = true
    }

    override fun handleUpdateRotation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {}

    override fun handleUpdateLocation(player: Player, to: Location, from: Location, packet: PacketPlayInFlying?) {

        if (from.blockX == to.blockX && from.blockZ == to.blockZ) {
            return
        }

        if (!GameEngine.instance.gameHandler.isPlaying(player)) {
            return
        }

        this.cache.removeIf{

            if (player.world.uid != it.location.world.uid || it.location.distance(player.location) > 5.0) {
                return@removeIf false
            }

            it.destroy()
            FrozenHologramHandler.getCache().remove(it)
            return@removeIf true
        }

    }

}