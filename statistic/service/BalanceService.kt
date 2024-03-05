package cc.fyre.bunkers.statistic.service

import cc.fyre.bunkers.Bunkers

import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer

/**
 * @project bunkers
 *
 * @date 05/08/2020
 * @author xanderume@gmail.com
 */
class BalanceService(private val instance: Bunkers) : Runnable {

    override fun run() {

        if (GameEngine.instance.gameHandler.getState() != GameServer.State.IN_PROGRESS) {
            return
        }

        GameEngine.instance.gameHandler.getPlayers().forEach{this.instance.statisticHandler.addBalance(it.uniqueId,3)}
    }

}