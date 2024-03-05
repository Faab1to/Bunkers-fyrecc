package cc.fyre.bunkers.game.deathmessage

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.deathmessage.DeathMessageConfiguration
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.ChatColor
import java.util.*

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
object BunkersDeathMessageConfiguration : DeathMessageConfiguration {

    override fun shouldShowDeathMessage(p0: UUID?, p1: UUID?, p2: UUID?): Boolean {
        return true
    }

    override fun formatPlayerName(uuid: UUID): String {
        return "${Bunkers.instance.teamHandler.findById(uuid)?.getColor() ?: ChatColor.WHITE}${UUIDUtils.name(uuid)}${ChatColor.DARK_RED}[${Bunkers.instance.statisticHandler.getKills(uuid)}]"
    }

}