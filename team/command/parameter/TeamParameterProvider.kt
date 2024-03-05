package cc.fyre.bunkers.team.command.parameter

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.command.ParameterType
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
class TeamParameterProvider : ParameterType<Team?> {

    override fun transform(sender: CommandSender,source: String): Team? {

        if (sender is Player && source.equals("self",true)) {

            val team = Bunkers.instance.teamHandler.findById(sender.uniqueId)

            if (team == null) {
                sender.sendMessage("${ChatColor.RED}You are not on a team!")
                return null
            }

            return team
        }

        var type: Team.Type? = null

        try {
            type = Team.Type.valueOf(source.toUpperCase())
        } catch (ex: Exception) {}

        if (type != null) {
            return Bunkers.instance.teamHandler.cache[type]!!
        }

        var id: UUID?

        val player = Bunkers.instance.server.getPlayer(source)

        if (player != null) {
            id = player.uniqueId
        }

        id = UUIDUtils.uuid(source)

        val team: Team? = if (id == null) null else Bunkers.instance.teamHandler.findById(id)

        if (team == null) {
            sender.sendMessage("${ChatColor.RED}No team or member with the name $source found.")
            return null
        }

        return team
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        return ArrayList()
    }

}