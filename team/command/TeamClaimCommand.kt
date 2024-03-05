package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers

import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.map.data.Map
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
object
TeamClaimCommand {

    @JvmStatic
    @Command(names = ["team claim","t claim","faction claim","fac claim","f claim"],hidden = true,permission = "bunkers.command.team.claim")
    fun execute(player: Player, @Param(name = "team")team: Team, @Param(name = "map")map: Map) {
        Bunkers.instance.claimHandler.startSelection(player,team,map,0)
    }


}