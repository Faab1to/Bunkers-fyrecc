package cc.fyre.bunkers.game.scoreboard

import cc.fyre.bunkers.Bunkers
import cc.fyre.engine.GameEngine
import cc.fyre.engine.game.adapter.GameAdapter
import cc.fyre.engine.game.adapter.scoreboard.ScoreboardAdapter
import net.frozenorb.qlib.scoreboard.ScoreGetter
import net.frozenorb.qlib.scoreboard.TitleGetter
import org.bukkit.ChatColor

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class CountdownScoreboardAdapter : ScoreboardAdapter {

    override fun getTitleGetter(): TitleGetter {
        return TitleGetter.forStaticString(
            "${ChatColor.GOLD}${ChatColor.BOLD} MC-Market ${ChatColor.GRAY}┃${ChatColor.WHITE} Bunkers"
        )
    }

    override fun getScoreGetter(): ScoreGetter {
        return ScoreGetter{toReturn,player ->

            val team = Bunkers.instance.teamHandler.findById(player.uniqueId) ?: return@ScoreGetter

            toReturn.add(GameAdapter.SCOREBOARD_LINE)
            toReturn.add("${ChatColor.GREEN}${ChatColor.BOLD}Map${ChatColor.GRAY}: ${ChatColor.WHITE}${GameEngine.instance.gameHandler.map.id}")
            toReturn.add("${ChatColor.GOLD}${ChatColor.BOLD}Team${ChatColor.GRAY}: ${team.getDisplayName()}")
            toReturn.add("${ChatColor.DARK_BLUE}${GameAdapter.SCOREBOARD_LINE}")

            toReturn.toTypedArray()
        }

    }

}