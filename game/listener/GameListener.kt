package cc.fyre.bunkers.game.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.game.BunkersGameAdapter
import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.GameEngine
import cc.fyre.engine.game.event.GameStateChangeEvent
import cc.fyre.engine.map.event.MapLoadEvent
import cc.fyre.engine.server.data.GameServer
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.StringUtil

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class GameListener(private val instance: Bunkers) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onMapLoad(event: MapLoadEvent) {
        event.world.setGameRuleValue("doMobSpawning","false")
        event.world.entities.filterIsInstance<Monster>().forEach{it.remove()}

        val handle = (event.world as CraftWorld).handle

        handle.spigotConfig.walkExhaustion = 0.0F
        handle.spigotConfig.regenExhaustion = 0.0F
        handle.spigotConfig.sprintExhaustion = 0.0F
        handle.spigotConfig.combatExhaustion = 0.0F
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private fun onFarmDecay(event: BlockFadeEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onPlayerDropItem(event: PlayerDropItemEvent) {

        val stack = event.itemDrop.itemStack

        if (!stack.hasItemMeta() || !stack.itemMeta.hasLore() || !ChatColor.stripColor(stack.itemMeta.lore[0]).equals("Soulbound",true)) {
            return
        }

        event.itemDrop.remove()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.entity !is Player) {
            return
        }

        val player = event.entity as Player

        if (player.activePotionEffects.none{it.type == PotionEffectType.INVISIBILITY && it.amplifier == 2}) {
            return
        }

        var attacker: Player? = null

        if (event.damager is Player) {
            attacker = event.damager as Player
        } else if (event.damager is Projectile && (event.damager as Projectile).shooter is Player) {
            attacker = (event.damager as Projectile).shooter as Player
        }

        if (attacker == null) {
            return
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        player.sendMessage("${ChatColor.RED}${ChatColor.RED}Your lesser invisibility has been removed!")
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        //TODO LunarClientAPI.getInstance().sendPacket(event.player, LCPacketServerRule(ServerRule.LEGACY_COMBAT, true))

        if (!GameEngine.instance.gameHandler.isPlaying(event.player)) {
            return
        }

        val team = this.instance.teamHandler.findById(event.player.uniqueId) ?: return

        if (!team.isRaidable()) {
            return
        }

        GameEngine.instance.gameHandler.removePlayer(event.player)
        GameEngine.instance.spectateHandler.addSpectator(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerInteract(event: PlayerInteractEvent) {

        if (event.action != Action.PHYSICAL) {
            return
        }

        if (event.clickedBlock.type != Material.SOIL) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onInventoryOpen(event: InventoryOpenEvent) {

        if (!DISABLED_INVENTORIES.any{it == event.inventory.type}) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onItemConsume(event: PlayerItemConsumeEvent) {

        if (event.isCancelled) {
            return
        }

        if (event.item.type != Material.POTION) {
            return
        }

        object : BukkitRunnable() {

            override fun run() {

                if (!event.player.isOnline) {
                    return
                }

                event.player.itemInHand = null
            }

        }.runTaskLater(this.instance,2L)

    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onGameStateChange(event: GameStateChangeEvent) {

        if (event.new != GameServer.State.IN_PROGRESS) {
            return
        }

        val koth = this.instance.teamHandler.cache[Team.Type.KOTH]!!

        if (koth.hq != null) {
            koth.hq!!.chunk.load()
        }

        this.instance.server.onlinePlayers.forEach{

            val team = this.instance.teamHandler.findById(it.uniqueId)

            if (team?.hq != null && !team.hq!!.chunk.isLoaded) {
                team.hq!!.chunk.load()
            }

            if (team == null) {
                it.teleport(koth.hq)
            } else {

                val hq = team.hq

                if (hq == null) {
                    it.sendMessage("${ChatColor.RED}Your team's HQ seems to be missing, please contact an administrator..")
                } else {
                    it.teleport(hq)
                    //TODO sendPacket(it,WayPointAddPacket("HQ",hq.world,hq.blockX,hq.blockY,hq.blockZ,team.type.getRGBColor(),forced = true,visible = true))

                    val teams = StringUtils.join(this.instance.teamHandler.findPlayerTeams().filter{ filtered -> filtered.type != team.type && filtered.members.isNotEmpty()}.map{ filtered -> filtered.getDisplayName()}.toTypedArray())

                    arrayListOf(
                            "",
                            " ${ChatColor.GOLD}You are apart of the ${team.getDisplayName()}${ChatColor.GOLD} team.",
                            " ${ChatColor.GOLD}Make $teams${ChatColor.GOLD} raidable",
                            " ${ChatColor.GOLD}and/or capture ${koth.getDisplayName()}${ChatColor.GOLD} to win.",
                            " ${ChatColor.GOLD}Make sure to block up all ${ChatColor.RED}${ChatColor.BOLD}${team.holograms.size} ${ChatColor.GOLD}entrances!",
                            " ${ChatColor.GOLD}Food can be found outside your base.",
                            ""
                    ).forEach{message -> it.sendMessage(message)}
                }

            }

            if (koth.hq != null) {
                //TODO sendPacket(it,WayPointAddPacket("${koth.getDisplayName()}${ChatColor.WHITE}",koth.hq!!.world,koth.hq!!.blockX,koth.hq!!.blockY,koth.hq!!.blockZ,koth.type.getRGBColor(),forced = true,visible = true))
            }

            BunkersGameAdapter.STARTER_ITEMS.forEach{item -> it.inventory.addItem(item)}
        }

        this.instance.teamHandler.findPlayerTeams().forEach{team -> team.shops.entries.forEach{this.instance.villagerHandler.cache.add(this.instance.shopHandler.spawnVillager(it.value,it.key,team.type))}}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onBlockPhysics(event: BlockPhysicsEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onGameStateChangeEnding(event: GameStateChangeEvent) {

        if (event.new != GameServer.State.ENDING) {
            return
        }

        val team = this.instance.eventHandler.controlledByTeam ?: return

        object : BukkitRunnable() {

            override fun run() {

                val koth = this@GameListener.instance.teamHandler.cache[Team.Type.KOTH]

                if (koth?.hq != null) {
                    koth.hq!!.world.spawnEntity(koth.hq,EntityType.FIREWORK) as Firework
                }

                Bukkit.broadcastMessage("${team.getDisplayName()}${ChatColor.YELLOW} wins!")
            }

        }.runTaskTimer(this.instance,30L,30L)
    }

    companion object {

        val DISABLED_INVENTORIES = InventoryType.values().filter{it != InventoryType.CHEST && it != InventoryType.CREATIVE && it != InventoryType.PLAYER}.toMutableSet()

    }

}