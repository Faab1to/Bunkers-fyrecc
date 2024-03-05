package cc.fyre.bunkers.pillar.data.adapter

import cc.fyre.bunkers.team.data.Team
import org.bukkit.entity.Player

import org.bukkit.Material

/**
 * @project hcf
 *
 * @date 02/01/2021
 * @author xanderume@gmail.com
 */
interface WallAdapter {

    fun getMaterial(player: Player):Pair<Material,Int>
    fun shouldCheck(player: Player,team: Team?):Boolean
    fun shouldApply(player: Player,team: Team,playerTeam: Team?):Boolean

}