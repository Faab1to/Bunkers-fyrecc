package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.GameEngine
import cc.fyre.engine.util.FormatUtil
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project bunkers
 *
 * @date 22/12/2020
 * @author xanderume@gmail.com
 */
object TeamRallyCommand {


    @JvmStatic
    @Command(names = ["faction rally","fac rally","f rally","team rally","t rally","rally"], permission = "")
    fun execute(player: Player,@Param(name = "team",defaultValue = "self")team: Team) {

        if (!GameEngine.instance.gameHandler.isPlaying(player)) {
            player.sendMessage("${ChatColor.RED}You cannot do this as a spectator!")
            return
        }

        if (team.rally != null) {
            //TODO team.findOnlineMembers().forEach{LunarClientAPI.instance.packetHandler.sendPacket(it,WayPointRemovePacket("Rally",team.rally!!.world))}
        }

        if (team.rallyTask != null) {
            team.rallyTask!!.cancel()
        }

        team.rally = player.location.clone().add(0.5,0.0,0.5)
        team.rallyTask = object : BukkitRunnable() {

            override fun run() {

                if (team.rally == null) {
                    return
                }

                //TODO team.findOnlineMembers().forEach{LunarClientAPI.instance.packetHandler.sendPacket(it,WayPointRemovePacket("Rally",team.rally!!.world))}
                team.rally = null
                team.rallyTask = null
                team.sendMessage("${ChatColor.DARK_AQUA}The team rally has expired!")
            }

        }.runTaskLater(Bunkers.instance,TeamHandler.RALLY_TIME * 20L)

        //TODO val packet = WayPointAddPacket("Rally",team.rally!!.world,team.rally!!.blockX,team.rally!!.blockY,team.rally!!.blockZ,Color.ORANGE,forced = true,visible = true)

        team.sendMessage("${ChatColor.DARK_AQUA}${player.name} has updated the team's rally point, this will last for ${FormatUtil.formatIntoDetailedString(TeamHandler.RALLY_TIME * 1000L)}!")
        //TODO team.findOnlineMembers().forEach{LunarClientAPI.instance.packetHandler.sendPacket(it,packet)}
    }
    
}