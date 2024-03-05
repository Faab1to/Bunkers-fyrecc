package cc.fyre.bunkers.event

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.event.command.KOTHSetTimeCommand
import cc.fyre.bunkers.event.command.KOTHStartCommand
import cc.fyre.bunkers.event.listener.EventListener
import cc.fyre.bunkers.event.service.EventService
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.team.listener.TeamProtectionListener
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.GameEngine
import net.frozenorb.qlib.command.FrozenCommandHandler
import net.frozenorb.qlib.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class EventHandler(private val instance: Bunkers) {

    private var active = false

    var controller: UUID? = null
    var maxControlTime = 480

    var controlledBy: UUID? = null
    var controlledByTeam: Team? = null

    val service = EventService(this.instance)

    init {
        HylistSpigot.INSTANCE.addMovementHandler(EventListener(this.instance))

        FrozenCommandHandler.registerClass(KOTHStartCommand::class.java)
        FrozenCommandHandler.registerClass(KOTHSetTimeCommand::class.java)

        this.instance.server.pluginManager.registerEvents(EventListener(this.instance),this.instance)
    }

    fun isActive():Boolean {
        return this.active
    }

    fun setActive(value: Boolean) {

        if (value) {
            this.service.runTaskTimer(this.instance,20L,20L)
            this.instance.server.broadcastMessage("$PREFIX ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW} can now be contested.")
        }

        this.active = value
    }

    fun setMaxCaptureTime(seconds: Int) {

        if (this.service.remaining.get() > seconds) {
            this.service.remaining.set(seconds)
        }

        this.maxControlTime = seconds
        this.instance.server.broadcastMessage("$PREFIX ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW} has been reduced to ${ChatColor.RED}${TimeUtils.formatIntoDetailedString(seconds)}${ChatColor.YELLOW}.")
    }

    fun getCaptureZone():Location {
        return this.instance.teamHandler.cache[Team.Type.KOTH]?.hq ?: this.instance.server.getWorld(GameEngine.instance.gameHandler.map.id).spawnLocation.clone().add(0.5,0.0,0.5)
    }

    fun setController(player: Player?) {

        if ((this.maxControlTime - this.service.remaining.get() > 30)) {
            this.instance.server.broadcastMessage("$PREFIX ${ChatColor.YELLOW}Control of ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW} lost.")
        }

        this.service.remaining.set(this.maxControlTime)

        this.controller = player?.uniqueId

        if (player == null) {
            this.service.remaining.set(this.maxControlTime)
            return
        }

        player.sendMessage("$PREFIX ${ChatColor.YELLOW}Attempting to control ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW}.")
    }

    fun isInsideCaptureZone(location: Location, player: Player):Boolean {

        val zone = this.getCaptureZone()

        if (!location.world.name.equals(zone.world.name,true)) {
            return false
        }

        if (TeamProtectionListener.protection.containsKey(player.uniqueId)) {
            return false
        }

        if (GameEngine.instance.spectateHandler.isSpectating(player.uniqueId)) {
            return false
        }

        if (GameEngine.instance.disqualifieHandler.isDisqualified(player.uniqueId)) {
            return false
        }

        val respawn = this.instance.timerHandler.findRemaining(player.uniqueId, TimerType.RESPAWN)

        if (respawn > 0L) {
            return false
        }

        var radius = 3

        if (location.world.name.equals("Classic",true)) {
            radius = 5
        }

        return abs(location.blockX - zone.blockX) <= radius && abs(location.blockY - zone.blockY) <= 5 && abs(location.blockZ - zone.blockZ) <= radius
    }

    companion object {

        val PREFIX = "${ChatColor.GOLD}[KingOfTheHill]"
        val KOTH_START_TIME = TimeUnit.MINUTES.toSeconds(10).toInt()
        val CAPTURE_TIME_DECREASE = mutableMapOf(
                TimeUnit.MINUTES.toSeconds(20).toInt() to 420,
                TimeUnit.MINUTES.toSeconds(30).toInt() to 360,
                TimeUnit.MINUTES.toSeconds(40).toInt() to 300,
                TimeUnit.MINUTES.toSeconds(50).toInt() to 240,
                TimeUnit.MINUTES.toSeconds(60).toInt() to 180
        )

    }
}