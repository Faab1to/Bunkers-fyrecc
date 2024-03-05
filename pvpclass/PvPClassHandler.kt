package cc.fyre.bunkers.pvpclass

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.pvpclass.data.PvPClass
import cc.fyre.bunkers.pvpclass.data.type.BardClass
import cc.fyre.bunkers.pvpclass.event.PvPClassEquipEvent
import cc.fyre.bunkers.pvpclass.event.PvPClassUnEquipEvent
import cc.fyre.bunkers.pvpclass.listener.PvPClassListener
import cc.fyre.engine.profile.data.type.BunkersProfile
import com.google.common.collect.HashBasedTable
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.math.min


/**
 * @project hcf
 *
 * @date 10/09/2020
 * @author xanderume@gmail.com
 */
class PvPClassHandler(private val instance: Bunkers) {

    val cache = HashMap<UUID,PvPClass.Type>()
    val typeToClass = HashMap<PvPClass.Type,PvPClass>()

    val effects = HashBasedTable.create<UUID,PotionEffectType,PotionEffect>()

    private val energy = HashMap<UUID,Long>()

    init {
        this.typeToClass[PvPClass.Type.BARD] = BardClass

        this.instance.server.pluginManager.registerEvents(PvPClassListener(this.instance),this.instance)
    }

    fun dispose() {
        this.instance.server.onlinePlayers.filter{this.cache.containsKey(it.uniqueId)}.forEach{this.setPvPClass(it,null)}
    }

    fun findById(uuid: UUID):PvPClass? {

        if (!this.cache.containsKey(uuid)) {
            return null
        }

        return this.typeToClass[this.cache[uuid]!!]
    }

    fun findByType(type: PvPClass.Type):PvPClass? {
        return this.typeToClass[type]
    }

    fun setPvPClass(player: Player,type: PvPClass.Type?) {

        if (this.cache.containsKey(player.uniqueId)) {

            if (this.cache[player.uniqueId] == type) {
                return
            }

            val previous = this.cache.remove(player.uniqueId)!!

            this.instance.server.pluginManager.callEvent(PvPClassUnEquipEvent(player,this.typeToClass[previous]!!))

            this.energy.remove(player.uniqueId)
            this.typeToClass[previous]!!.onDisable(player)

            this.instance.statisticHandler.recalculatePlayTime(player.uniqueId,previous.backendType)
            this.instance.statisticHandler.playTimeClassEquipped[player.uniqueId]?.remove(previous.backendType)

            previous.effects.filter{player.activePotionEffects.any{effect -> it.type == effect.type && it.duration > (TimeUnit.MINUTES.toMillis(8L)) && it.amplifier == effect.amplifier}}.forEach{player.removePotionEffect(it.type)}
        }

        if (type == null || !this.typeToClass.containsKey(type)) {
            return
        }

        val event = PvPClassEquipEvent(player,this.typeToClass[type]!!)

        this.instance.server.pluginManager.callEvent(event)

        if (event.isCancelled) {
            return
        }

        this.cache[player.uniqueId] = type
        this.energy[player.uniqueId] = System.currentTimeMillis()
        this.typeToClass[type]!!.onEnable(player)


        this.instance.statisticHandler.playTimeClassEquipped.putIfAbsent(player.uniqueId,EnumMap(BunkersProfile.PvPClass::class.java))
        this.instance.statisticHandler.playTimeClassEquipped[player.uniqueId]!![type.backendType] = System.currentTimeMillis()

        type.effects.forEach{player.addPotionEffect(it,true)}
    }

    fun getEnergy(player: Player):Int {

        if (!this.energy.containsKey(player.uniqueId)) {
            return 0
        }

        val energy = this.energy[player.uniqueId]!!

        if (energy == 0L) {
            return 0
        }

        return min(100,(1.0 * (System.currentTimeMillis()) - energy).toInt() / 1000)
    }

    fun setEnergy(player: Player,energy: Int) {
        this.energy[player.uniqueId] = (System.currentTimeMillis() - (1000L * energy))
    }

    fun addPotionEffect(player: Player,effect: PotionEffect) {

        if (player.activePotionEffects.any{it.type == effect.type && (it.amplifier > effect.amplifier || (it.amplifier == effect.amplifier && it.duration > effect.duration))}) {
            return
        }

        // only cache the ones are lower then infinite & normal bard speed duration
        player.activePotionEffects.stream().filter{it.type == effect.type && it.duration > 120 && it.duration <= 1_000_000}.findFirst().ifPresent{this.effects.put(player.uniqueId,it.type,it)}

        player.addPotionEffect(effect,true)
    }

    companion object {

        const val BARD_RANGE = 25.0

    }
}