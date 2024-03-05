package cc.fyre.bunkers.game.scoreboard

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.GameEngine
import cc.fyre.engine.game.adapter.GameAdapter
import cc.fyre.engine.game.adapter.scoreboard.ScoreboardAdapter
import cc.fyre.engine.server.data.GameServer
import cc.fyre.engine.util.FormatUtil
import net.frozenorb.qlib.scoreboard.ScoreGetter
import net.frozenorb.qlib.scoreboard.TitleGetter
import org.bukkit.ChatColor

/**
 * @project bunkers
 *
 * @date 14/08/2020
 * @author xanderume@gmail.com
 */
class InProgressScoreboardAdapter : ScoreboardAdapter {

    override fun getTitleGetter(): TitleGetter {
        return TitleGetter.forStaticString(
            "${ChatColor.GOLD}${ChatColor.BOLD} MC-Market ${ChatColor.GRAY}┃${ChatColor.WHITE} Bunkers"
        )
    }

    override fun getScoreGetter(): ScoreGetter {
        return ScoreGetter{toReturn,player ->

            if (GameEngine.instance.gameHandler.getState() == GameServer.State.VOTING) {
                GameEngine.instance.voteHandler.cache.entries.forEach{toReturn.add("${ChatColor.DARK_AQUA}${ChatColor.BOLD}${it.key.id}${ChatColor.GRAY}: ${ChatColor.WHITE}${it.value.size}")}
            }

            if (GameEngine.instance.gameHandler.getState() == GameServer.State.IN_PROGRESS) {
                toReturn.add("${ChatColor.GOLD}${ChatColor.BOLD}Game Time${ChatColor.GRAY}: ${ChatColor.RED}${FormatUtil.formatIntoMMSS(GameEngine.instance.gameHandler.getGameTime())}")
            }

            val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

            if (team != null) {
                toReturn.add("${ChatColor.GOLD}${ChatColor.BOLD}DTR${ChatColor.GRAY}: ${team.getDTRDisplay()}")
            }

            if (!GameEngine.instance.spectateHandler.isSpectating(player)) {
                toReturn.add("${ChatColor.GREEN}${ChatColor.BOLD}Balance${ChatColor.GRAY}:${ChatColor.RED} $${Bunkers.instance.statisticHandler.getBalance(player.uniqueId)}")
            }

            if (Bunkers.instance.eventHandler.isActive()) {
                toReturn.add("${ChatColor.BLUE}${ChatColor.BOLD}${GameEngine.instance.gameHandler.map.id}${ChatColor.GRAY}: ${ChatColor.RED}${FormatUtil.formatIntoMMSS(Bunkers.instance.eventHandler.service.remaining.get() * 1000L)}")
            }

            Bunkers.instance.timerHandler.findTimers(player.uniqueId).filter{it.type.displays}.forEach{toReturn.add("${it.type.scoreboard}${ChatColor.GRAY}:${ChatColor.RED} ${FormatUtil.formatIntoFancy(it.getRemaining())}")}


            val pvpClass = Bunkers.instance.pvpClassHandler.findById(player.uniqueId)

            if (pvpClass != null) {

                if (pvpClass.isEnergyBased()) {

                    val cooldown = Bunkers.instance.timerHandler.findRemaining(player.uniqueId,TimerType.ENERGY_COOLDOWN)

                    if (cooldown > 0L) {
                        toReturn.add("${ChatColor.GREEN}${ChatColor.BOLD}${pvpClass.getName()} Effect${ChatColor.GRAY}: ${ChatColor.RED}${FormatUtil.formatIntoMMSS(cooldown)}")
                    }

                    val energy = Bunkers.instance.pvpClassHandler.getEnergy(player)

                    if (energy > 0) {
                        toReturn.add("${ChatColor.AQUA}${ChatColor.BOLD}${pvpClass.getName()} Energy${ChatColor.GRAY}: ${ChatColor.RED}$energy.0")
                    }

                }

            }

            if (toReturn.isNotEmpty()) {
                toReturn.add(0,GameAdapter.SCOREBOARD_LINE)
                toReturn.add("${ChatColor.DARK_BLUE}${GameAdapter.SCOREBOARD_LINE}")
            }

            toReturn.toTypedArray()
        }
    }

}
