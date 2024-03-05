package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import mkremins.fanciful.FancyMessage
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.collections.ArrayList

/**
 * @project bunkers
 *
 * @date 24/08/2020
 * @author xanderume@gmail.com
 */
object TeamVoteKickCommand {

    @JvmStatic
    @Command(names = ["faction votekick","fac votekick","f votekick","team votekick","t votekick","votekick","faction votekick","fac votekick","f votekick","team votekick","t votekick","votekick"], permission = "")
    fun execute(player: Player,@Param(name = "player")target: Player) {

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }

        if (GameEngine.instance.gameHandler.getState().isBeforeOrCurrently(GameServer.State.COUNTDOWN)) {
            player.sendMessage("${ChatColor.RED}You cannot votekick players as the game has not started yet!")
            return
        }

        if (team.members.size <= 2) {
            player.sendMessage("${ChatColor.RED}Members can no longer be vote kicked.")
            return
        }

        if (!team.isMember(target)) {
            player.sendMessage("${ChatColor.WHITE}${target.name} ${ChatColor.RED}is not on your team.")
            return
        }

        if (GameEngine.instance.spectateHandler.isSpectating(player) || GameEngine.instance.disqualifieHandler.isDisqualified(player.uniqueId)) {
            player.sendMessage("${ChatColor.RED}You can no longer vote kick a member.")
            return
        }

        if (player.uniqueId == target.uniqueId) {
            player.sendMessage("${ChatColor.RED}You cannot vote kick yourself.")
            return
        }

        if (GameEngine.instance.spectateHandler.isSpectating(target) || GameEngine.instance.disqualifieHandler.isDisqualified(target.uniqueId)) {
            player.sendMessage("${ChatColor.RED}You can no longer vote kick ${ChatColor.WHITE}${target.name}${ChatColor.RED}.")
            return
        }

        team.votes.putIfAbsent(target.uniqueId,ArrayList())

        if (team.votes[target.uniqueId]!!.contains(player.uniqueId)) {
            player.sendMessage("${ChatColor.RED}You have already voted to kick ${target.name})}${ChatColor.RED}.")
            return
        }

        val required = team.findAliveMembers().size - 1

        team.votes[target.uniqueId]!!.add(player.uniqueId)

        if (team.votes[target.uniqueId]!!.size >= required) {
            team.dtr -= 1.0
            team.votes[target.uniqueId]?.clear()

            team.sendMessage("${ChatColor.RED}${target.name} has been voted off the team!",target.uniqueId)
            target.sendMessage("${ChatColor.RED}You have been voted off the team.")

            GameEngine.instance.gameHandler.removePlayer(target)
            GameEngine.instance.spectateHandler.addSpectator(target)

            Bunkers.instance.statisticHandler.recalculatePlayTime(target.uniqueId)
            Bunkers.instance.statisticHandler.playTimeJoined.remove(target.uniqueId)

            Bunkers.instance.server.onlinePlayers.filterNot{team.isMember(it)}.forEach{it.sendMessage("${team.getColor()}${target.name}${ChatColor.RED} has been voted off ${team.getDisplayName()}${ChatColor.RED}'s team!")}
            return
        }

        team.members.mapNotNull{Bunkers.instance.server.getPlayer(it)}.forEach{

            val message = FancyMessage("${team.getColor()}[Team] ${ChatColor.RED}${player.name}${ChatColor.YELLOW} has voted to kick ${ChatColor.RED}${target.name}${ChatColor.YELLOW}.")

            if (it.uniqueId != target.uniqueId) {
                message.tooltip("${ChatColor.GREEN}Click to vote kick ${player.name}.")
            }

            message.send(it)
        }

    }

}