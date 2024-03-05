package cc.fyre.bunkers

import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.claim.ClaimHandler
import cc.fyre.bunkers.event.EventHandler
import cc.fyre.bunkers.game.BunkersGameAdapter
import cc.fyre.bunkers.game.tab.BunkersTabAdapter
import cc.fyre.bunkers.pvpclass.PvPClassHandler
import cc.fyre.bunkers.repair.RepairHandler
import cc.fyre.bunkers.shop.ShopHandler
import cc.fyre.bunkers.villager.VillagerHandler
import cc.fyre.bunkers.statistic.StatisticHandler
import cc.fyre.bunkers.supply.SupplyHandler
import cc.fyre.bunkers.pillar.PillarHandler

import cc.fyre.bunkers.timer.TimerHandler
import cc.fyre.engine.GameEngine
import net.frozenorb.qlib.tab.FrozenTabHandler

import org.bukkit.plugin.java.JavaPlugin

/**
 * @project bunkers
 *
 * @date 05/03/24
 * @author Faab1to
 */
class Bunkers : JavaPlugin() {

    lateinit var adapter: BunkersGameAdapter

    lateinit var shopHandler: ShopHandler
    lateinit var teamHandler: TeamHandler
    lateinit var claimHandler: ClaimHandler
    lateinit var timerHandler: TimerHandler
    lateinit var eventHandler: EventHandler
    lateinit var supplyHandler: SupplyHandler
    lateinit var repairHandler: RepairHandler
    lateinit var pillarHandler: PillarHandler
    lateinit var villagerHandler: VillagerHandler
    lateinit var pvpClassHandler: PvPClassHandler
    lateinit var statisticHandler: StatisticHandler

    override fun onEnable() {
        instance = this

        this.adapter = BunkersGameAdapter(this)

        GameEngine.instance.gameHandler.adapter = this.adapter

        this.shopHandler = ShopHandler(this)
        this.teamHandler = TeamHandler(this)
        this.claimHandler = ClaimHandler(this)
        this.eventHandler = EventHandler(this)
        this.timerHandler = TimerHandler(this)
        this.supplyHandler = SupplyHandler(this)
        this.repairHandler = RepairHandler(this)
        this.pillarHandler = PillarHandler(this)
        this.villagerHandler = VillagerHandler(this)
        this.pvpClassHandler = PvPClassHandler(this)
        this.statisticHandler = StatisticHandler(this)

        FrozenTabHandler.setLayoutProvider(BunkersTabAdapter)
    }


    override fun onDisable() {
        this.claimHandler.dispose()
        this.supplyHandler.dispose()
        this.pvpClassHandler.dispose()
    }

    companion object {

        lateinit var instance: Bunkers

    }
}