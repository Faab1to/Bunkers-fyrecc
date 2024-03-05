package cc.fyre.bunkers.game.tab

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.event.EventHandler
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.timer.data.TimerType

import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import cc.fyre.engine.util.FormatUtil
import com.google.common.collect.HashBasedTable
import net.frozenorb.qlib.tab.construct.TabLayout
import net.frozenorb.qlib.tab.provider.LayoutProvider
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

/**
 * @project bunkers
 *
 * @date 21/08/2020
 * @author xanderume@gmail.com
 */
object BunkersTabAdapter : LayoutProvider {

    override fun provide(player: Player): TabLayout {

        val layout = TabLayout.create(player)

        var y = 3

        layout.set(0,y++,"${ChatColor.GOLD}Player Info:")
        layout.set(0,y++,"${ChatColor.GRAY}Kills: ${ChatColor.WHITE}${Bunkers.instance.statisticHandler.getKills(player.uniqueId)}")
        layout.set(0,y++,"${ChatColor.GRAY}Deaths: ${ChatColor.WHITE}${Bunkers.instance.statisticHandler.getDeaths(player.uniqueId)}")

        y++

        if (player.location.world != null) {
            layout.set(0,y++,"${ChatColor.GOLD}Location:")
            layout.set(0,y++,"${ChatColor.RED}${Bunkers.instance.teamHandler.findByLocation(player.location).getDisplayName()}")
            layout.set(0,y++,"${ChatColor.GRAY}${player.location.blockX}, ${player.location.blockZ} [${this.getCardinalDirection(player)}${ChatColor.GRAY}]")
        }

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team != null) {
            y = 3

            layout.set(1,y++,"${ChatColor.GOLD}Team Info:")
            layout.set(1,y++,"${ChatColor.GRAY}DTR: ${team.getDTRDisplay()}")
            layout.set(1,y++,"${ChatColor.GRAY}Online: ${ChatColor.WHITE}${team.findOnlineMembers().size}/${team.members.size}")
        }
        
        y = 3

        layout.set(2,y++,"${ChatColor.GOLD}Game Info:")

        if (GameEngine.instance.gameHandler.getState().isPastOrCurrently(GameServer.State.IN_PROGRESS)) {
            layout.set(2,y++,"${ChatColor.GRAY}Time: ${ChatColor.WHITE}${FormatUtil.formatIntoMMSS(GameEngine.instance.gameHandler.getGameTime())}")
        } else {

            var map = "Voting"

            if (GameEngine.instance.gameHandler.getState().isPastOrCurrently(GameServer.State.COUNTDOWN)) {
                map = GameEngine.instance.gameHandler.map.id
            }

            layout.set(2,y++,"${ChatColor.GRAY}Map: ${ChatColor.WHITE}$map")
        }

        layout.set(2,y++,"${ChatColor.GRAY}Players: ${ChatColor.WHITE}${GameEngine.instance.gameHandler.getPlayers().size}")

        y++

        if (GameEngine.instance.gameHandler.getState().isPastOrCurrently(GameServer.State.COUNTDOWN)) {
            val captureZone = Bunkers.instance.eventHandler.getCaptureZone()

            layout.set(2,y++,"${ChatColor.GOLD}${GameEngine.instance.gameHandler.map.id}:")
            layout.set(2,y++,"${ChatColor.GRAY}${captureZone.blockX}, ${captureZone.blockZ}")

            val time = if (Bunkers.instance.eventHandler.isActive()) {
                Bunkers.instance.eventHandler.service.remaining.get() * 1000L
            } else {
                (EventHandler.KOTH_START_TIME * 1000L) - (GameEngine.instance.gameHandler.getGameTime())
            }

            layout.set(2,y++,"${ChatColor.GRAY}${FormatUtil.formatIntoMMSS(time)}")
        }

        y = 7

        var x = 1

        Bunkers.instance.teamHandler.findPlayerTeams().sortedByDescending{it.isMember(player)}.forEach{

            val next: Int = if (y == 7) {
                0
            } else {
                if (x >= 2) {
                    0
                } else {
                    x + 1
                }
            }

            layout.set(x,y++,"${it.getDisplayName()} Team ${ChatColor.GRAY}[${it.getDTRDisplay()}${ChatColor.GRAY}]")

            it.members.forEach{member -> layout.set(x,y++,this.getDisplayName(player,member,it))}

            y = 14
            x = next
        }

        return layout
    }

    private fun getCardinalDirection(player: Player):String {

        var rot = (player.location.yaw - 90) % 360.toDouble()

        if (rot < 0) {
            rot += 360.0
        }

        return this.getDirection(rot)
    }

    private fun getDirection(rot: Double): String {

        return when {
            0 <= rot && rot < 22.5 -> Direction.WEST.getDisplay()
            22.5 <= rot && rot < 67.5 -> "${Direction.NORTH.getDisplay()}${Direction.WEST.getDisplay()}"
            67.5 <= rot && rot < 112.5 -> Direction.NORTH.getDisplay()
            112.5 <= rot && rot < 157.5 -> "${Direction.NORTH.getDisplay()}${Direction.EAST.getDisplay()}"
            157.5 <= rot && rot < 202.5 -> Direction.EAST.getDisplay()
            202.5 <= rot && rot < 247.5 -> "${Direction.SOUTH.getDisplay()}${Direction.EAST.getDisplay()}"
            247.5 <= rot && rot < 292.5 -> Direction.SOUTH.getDisplay()
            292.5 <= rot && rot < 337.5 -> "${Direction.SOUTH.getDisplay()}${Direction.WEST.getDisplay()}"
            337.5 <= rot && rot < 360.0 -> Direction.WEST.getDisplay()
            else -> ""
        }

    }
    private fun getDisplayName(viewer: Player,uuid: UUID,team: Team): String {

        val player = Bunkers.instance.server.getPlayer(uuid)

        if (player != null) {

            if (GameEngine.instance.spectateHandler.isSpectating(player)) {
                return "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${player.name}"
            }

            val respawn = Bunkers.instance.timerHandler.findRemaining(uuid,TimerType.RESPAWN)

            if (team.isMember(viewer.uniqueId) && respawn > 0L) {
                return "${ChatColor.GRAY}${player.name}${ChatColor.RED} ${respawn / 1000L}s"
            }

            return "${team.getColor()}${player.name}"
        }

        val name = UUIDUtils.name(uuid)

        if (GameEngine.instance.disqualifieHandler.isDisqualified(uuid)) {
            return "${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}${name}"
        }

        return "${ChatColor.GRAY}$name"
    }

    enum class Direction(private val display: String) {

        EAST("E"),
        WEST("W"),
        SOUTH("S"),
        NORTH("N");

        fun getDisplay():String {

            if (GameEngine.instance.gameHandler.getState().isBeforeOrCurrently(GameServer.State.COUNTDOWN)) {
                return "${ChatColor.WHITE}${this.display}"
            }

            return "${DIRECTIONS.get(GameEngine.instance.gameHandler.map.id.toLowerCase(),this) ?: ChatColor.WHITE}${this.display}"
        }

    }

    val DIRECTIONS = HashBasedTable.create<String,Direction,ChatColor>()

    init {
        //TODO LOL
        DIRECTIONS.put("western",Direction.WEST,ChatColor.BLUE)
        DIRECTIONS.put("western",Direction.EAST,ChatColor.YELLOW)
        DIRECTIONS.put("western",Direction.SOUTH,ChatColor.RED)
        DIRECTIONS.put("western",Direction.NORTH,ChatColor.GREEN)

        DIRECTIONS.put("classic",Direction.WEST,ChatColor.YELLOW)
        DIRECTIONS.put("classic",Direction.EAST,ChatColor.GREEN)
        DIRECTIONS.put("classic",Direction.SOUTH,ChatColor.RED)
        DIRECTIONS.put("classic",Direction.NORTH,ChatColor.BLUE)

        DIRECTIONS.put("medieval",Direction.WEST,ChatColor.GREEN)
        DIRECTIONS.put("medieval",Direction.EAST,ChatColor.YELLOW)
        DIRECTIONS.put("medieval",Direction.NORTH,ChatColor.RED)
        DIRECTIONS.put("medieval",Direction.SOUTH,ChatColor.BLUE)

        DIRECTIONS.put("colosseum",Direction.WEST,ChatColor.YELLOW)
        DIRECTIONS.put("colosseum",Direction.EAST,ChatColor.GREEN)
        DIRECTIONS.put("colosseum",Direction.NORTH,ChatColor.RED)
        DIRECTIONS.put("colosseum",Direction.SOUTH,ChatColor.BLUE)
    }

}