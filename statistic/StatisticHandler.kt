package cc.fyre.bunkers.statistic

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.statistic.command.BalanceCommand
import cc.fyre.bunkers.statistic.command.PayCommand
import cc.fyre.bunkers.statistic.listener.StatisticListener
import cc.fyre.bunkers.statistic.service.BalanceService
import cc.fyre.engine.profile.data.type.BunkersProfile
import net.frozenorb.qlib.command.FrozenCommandHandler
import org.bukkit.Material
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
class StatisticHandler(private val instance: Bunkers) {

    val kills = HashMap<UUID,Int>()
    val deaths = HashMap<UUID,Int>()
    val balance = HashMap<UUID,Int>()
    val killStreak = HashMap<UUID,Int>()

    val oresMined = HashMap<UUID,EnumMap<BunkersProfile.Ore,Int>>()

    val playTime = HashMap<UUID,Long>()
    val playTimeJoined = HashMap<UUID,Long>()

    val playTimeClass = HashMap<UUID,EnumMap<BunkersProfile.PvPClass,Long>>()
    val playTimeClassEquipped = HashMap<UUID,EnumMap<BunkersProfile.PvPClass,Long>>()

    init {
        FrozenCommandHandler.registerClass(PayCommand::class.java)
        FrozenCommandHandler.registerClass(BalanceCommand::class.java)

        this.instance.server.pluginManager.registerEvents(StatisticListener(this.instance),this.instance)

        this.instance.server.scheduler.runTaskTimer(this.instance,BalanceService(this.instance),60L,60L)
    }

    fun getKills(uuid: UUID):Int {
        return this.kills[uuid] ?: 0
    }

    fun addKills(uuid: UUID,amount: Int) {
        this.kills.putIfAbsent(uuid,0)
        this.kills[uuid] = this.kills[uuid]!! + amount
    }

    fun getDeaths(uuid: UUID):Int {
        return this.deaths[uuid] ?: 0
    }

    fun addDeaths(uuid: UUID,amount: Int) {
        this.deaths.putIfAbsent(uuid,0)
        this.deaths[uuid] = this.deaths[uuid]!! + amount
    }

    fun getBalance(uuid: UUID):Int {
        return this.balance[uuid] ?: STARTER_BALANCE
    }

    fun addBalance(uuid: UUID,amount: Int) {
        this.balance.putIfAbsent(uuid,STARTER_BALANCE)
        this.balance[uuid] = this.balance[uuid]!! + amount
    }

    fun getOresMined(uuid: UUID):Int {

        if (!this.oresMined.containsKey(uuid)) {
            return 0
        }

        return this.oresMined[uuid]!!.entries.sumBy{it.value}
    }

    fun getOresMined(uuid: UUID,ore: BunkersProfile.Ore):Int {

        if (!this.oresMined.containsKey(uuid)) {
            return 0
        }

        return this.oresMined[uuid]!![ore] ?: 0
    }

    fun addOresMined(uuid: UUID,material: Material) {
        this.oresMined.putIfAbsent(uuid,EnumMap(BunkersProfile.Ore::class.java))

        val ore = when(material) {
            Material.COAL_ORE -> BunkersProfile.Ore.COAL
            Material.IRON_ORE -> BunkersProfile.Ore.IRON
            Material.GOLD_ORE -> BunkersProfile.Ore.GOLD
            Material.DIAMOND_ORE -> BunkersProfile.Ore.DIAMOND
            Material.EMERALD_ORE -> BunkersProfile.Ore.EMERALD
            else -> null
        } ?: return

        this.oresMined[uuid]!![ore] = (this.oresMined[uuid]!![ore] ?: 0) + 1
    }

    fun getPlayTime(uuid: UUID):Long {
        return this.playTime[uuid] ?: 0L
    }

    fun getPlayTime(uuid: UUID,type: BunkersProfile.PvPClass):Long {

        if (!this.playTimeClass.containsKey(uuid)) {
            return 0L
        }

        return this.playTimeClass[uuid]!![type] ?: 0L
    }

    fun recalculatePlayTime(uuid: UUID) {

        if (!this.playTimeJoined.containsKey(uuid)) {
            return
        }

        this.playTime[uuid] = (this.playTime[uuid] ?: 0) + (System.currentTimeMillis() - this.playTimeJoined[uuid]!!)
    }

    fun recalculatePlayTime(uuid: UUID,pvpClass: BunkersProfile.PvPClass) {

        if (!this.playTimeClassEquipped.containsKey(uuid)) {
            return
        }

        this.playTimeClass.putIfAbsent(uuid,EnumMap(BunkersProfile.PvPClass::class.java))
        this.playTimeClass[uuid]!![pvpClass] = (this.playTimeClass[uuid]!![pvpClass] ?: 0) + (System.currentTimeMillis() - (this.playTimeClassEquipped[uuid]!![pvpClass] ?: 0L))
    }

    fun getKillStreak(uuid: UUID):Int {
        this.killStreak.putIfAbsent(uuid,0)
        return this.killStreak[uuid]!!
    }

    fun addKillStreak(uuid: UUID,amount: Int) {
        this.killStreak.putIfAbsent(uuid,0)
        this.killStreak[uuid] = this.killStreak[uuid]!! + amount
    }

    companion object {

        const val STARTER_BALANCE = 150
        const val BALANCE_PER_KILL = 200

        val PAY_TIME = TimeUnit.MINUTES.toMillis(2L)
    }

}