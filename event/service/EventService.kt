package cc.fyre.bunkers.event.service

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.event.EventHandler

import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import net.frozenorb.qlib.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.atomic.AtomicInteger

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class EventService(private val instance: Bunkers) : BukkitRunnable() {

    val remaining = AtomicInteger(480)

    override fun run() {

        if (this.instance.eventHandler.controller == null) {
            return
        }

        if (GameEngine.instance.gameHandler.getState() != GameServer.State.IN_PROGRESS) {
            return
        }

        val player = this.instance.server.getPlayer(this.instance.eventHandler.controller)

        if (player == null || !this.instance.eventHandler.isInsideCaptureZone(player.location, player) || player.isDead) {
            this.instance.eventHandler.setController(GameEngine.instance.gameHandler.getPlayers().filter{it.uniqueId != this.instance.eventHandler.controller}.firstOrNull{this.instance.eventHandler.isInsideCaptureZone(it.location, it)})
            return
        }

        if (this.remaining.get() <= 0) {
            this.cancel()
            this.instance.eventHandler.controlledBy = this.instance.eventHandler.controller!!
            this.instance.eventHandler.controlledByTeam = this.instance.teamHandler.findById(this.instance.eventHandler.controller!!)
            this.instance.eventHandler.setActive(false)
            GameEngine.instance.gameHandler.setState(GameServer.State.ENDING)
            return
        }

        if (this.remaining.get() != this.instance.eventHandler.maxControlTime && this.remaining.get() % 30 == 0) {

            val team = this.instance.teamHandler.findById(this.instance.eventHandler.controller!!)!!

            team.sendMessage("${EventHandler.PREFIX}${ChatColor.YELLOW} Your team is controlling ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW}.",this.instance.eventHandler.controller!!)

            this.instance.server.getPlayer(this.instance.eventHandler.controller).sendMessage("${EventHandler.PREFIX}${ChatColor.YELLOW} Attempting to control ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW}.")

            val ignore = team.members.plus(this.instance.eventHandler.controller)

            this.instance.server.onlinePlayers.filter{!ignore.contains(it.uniqueId)}.forEach{
                it.sendMessage(ChatColor.GOLD.toString() + "${EventHandler.PREFIX} ${ChatColor.BLUE}${GameEngine.instance.gameHandler.map.id}${ChatColor.YELLOW} is trying to be controlled. ${ChatColor.RED}(${TimeUtils.formatIntoHHMMSS(this.remaining.get())})")
            }

        }

        this.remaining.decrementAndGet()
    }

}