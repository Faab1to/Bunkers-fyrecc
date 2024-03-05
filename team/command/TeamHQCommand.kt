package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import net.frozenorb.qlib.command.Command
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
object TeamHQCommand {

    @JvmStatic
    @Command(names = [
        "faction hq","fac hq","f hq","team hq","t hq","hq","faction home","fac home","f home","team home","t home","home",
        "faction stuck","fac stuck","f stuck","team stuck","t stuck","stuck","faction stuck","fac stuck","f stuck","team stuck","t stuck","stuck"
    ],permission = "")
    fun execute(player: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        if (team.hq == null) {
            player.sendMessage("${ChatColor.RED}Your team's HQ has not been setup, please contact an administrator.")
            return
        }

        if (GameEngine.instance.gameHandler.getState() != GameServer.State.IN_PROGRESS) {
            player.sendMessage("${ChatColor.RED}You cannot warp to your team's HQ right now.")
            return
        }

        if (Bunkers.instance.timerHandler.hasTimer(player.uniqueId,TimerType.HOME)) {
            player.sendMessage("${ChatColor.RED}You are already warping to your team's HQ!")
            return
        }

        if (Bunkers.instance.timerHandler.hasTimer(player.uniqueId,TimerType.RESPAWN)) {
            player.sendMessage("${ChatColor.RED}You cannot warp to your team's HQ whilst respawning!")
            return
        }

        val teamByLocation = Bunkers.instance.teamHandler.findByLocation(player.location)

        val duration = if (teamByLocation.type == team.type || teamByLocation.type == Team.Type.WAR_ZONE || teamByLocation.type == Team.Type.KOTH || team.isRaidable()) 10_000L else 30_000L

        Bunkers.instance.timerHandler.addTimer(player.uniqueId,TimerType.HOME,duration)
    }

}