package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.command.CommandSender

/**
 * @project bunkers
 *
 * @date 08/08/2020
 * @author xanderume@gmail.com
 */
object TeamInfoCommand {

    @JvmStatic
    @Command(names = ["faction info","fac info","f info","team info","t info","faction who","fac who","f who","team who","t who","faction show","fac show","f show","team show","t show","faction i","fac i","f i","team i","t i"], permission = "")
    fun execute(sender: CommandSender,@Param(name = "team",defaultValue = "self")team: Team) {
        team.sendInfo(sender)
    }

}