package cc.fyre.bunkers.game.scoreboard

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
class VotingScoreboardAdapter : ScoreboardAdapter {

    override fun getTitleGetter(): TitleGetter {
        return TitleGetter.forStaticString(
            "${ChatColor.GOLD}${ChatColor.BOLD} MC-Market ${ChatColor.GRAY}┃${ChatColor.WHITE} Bunkers"
        )
    }

    override fun getScoreGetter(): ScoreGetter {
        return ScoreGetter{toReturn,_ ->


            toReturn.add(GameAdapter.SCOREBOARD_LINE)

            GameEngine.instance.voteHandler.cache.entries.sortedBy{it.key.id.length}.forEach{toReturn.add("${it.key.id}: ${ChatColor.GOLD}${it.value.size}")}

            toReturn.add("${ChatColor.DARK_BLUE}${GameAdapter.SCOREBOARD_LINE}")
            toReturn.toTypedArray()
        }
    }


}