package cc.fyre.bunkers.pvpclass.data.item.energy

import cc.fyre.bunkers.Bunkers

import cc.fyre.bunkers.pvpclass.PvPClassHandler
import cc.fyre.bunkers.pvpclass.data.PvPClass
import cc.fyre.bunkers.pvpclass.data.item.ConsumableItem
import cc.fyre.bunkers.pvpclass.data.type.BardClass
import cc.fyre.bunkers.pvpclass.event.type.BardEffectEvent
import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.util.PotionUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * @project hcf
 *
 * @date 11/09/2020
 * @author xanderume@gmail.com
 */
open class EnergyEffect(val energy: Int,material: Material,val effect: PotionEffect?) : ConsumableItem(material,TimeUnit.SECONDS.toMillis(10L)) {

    override fun onConsume(player: Player): Boolean {

        if (this.effect == null) {
            return false
        }

        player.addPotionEffect(this.effect,true)
        return true
    }

    class BardEffect(energy: Int,material: Material,val passive: PotionEffect?,effect: PotionEffect?) : EnergyEffect(energy,material,effect) {

        var predicate: Predicate<Player>? = null

        constructor(energy: Int,material: Material,passive: PotionEffect?,effect: PotionEffect?,predicate: Predicate<Player>):this(energy,material,passive,effect) {
            this.predicate = predicate
        }

        override fun onConsume(player: Player): Boolean {

            val debuff = if (this.effect == null) false else isDebuff(this.effect.type)
            val team = Bunkers.instance.teamHandler.findById(player.uniqueId) ?: return false

            val receivers = findReceivers(player,team,PvPClassHandler.BARD_RANGE,debuff,this.effect)

            Bunkers.instance.server.pluginManager.callEvent(BardEffectEvent(player,receivers.filter{it.uniqueId != player.uniqueId},this))

            receivers.forEach{

                if (this.effect == null && this.predicate != null) {
                    this.predicate!!.test(it)
                } else if (this.effect != null) {
                    Bunkers.instance.pvpClassHandler.addPotionEffect(it,this.effect)
                }

            }
            return true
        }

        companion object {

            fun isDebuff(effect: PotionEffectType):Boolean {
                return PotionUtil.isDebuff(effect)
            }

            fun findReceivers(player: Player,team: Team,range: Double,debuff: Boolean,effect: PotionEffect?):Collection<Player> {

                val toReturn = ArrayList<Player>()

                player.getNearbyEntities(range,range / 2,range).filter{it.uniqueId != player.uniqueId}.filterIsInstance<Player>().filter{

                    val pvpClass = Bunkers.instance.pvpClassHandler.findById(it.uniqueId)

                    if (effect != null && pvpClass?.type == PvPClass.Type.BARD && BardClass.BARD_ILLEGAL_EFFECTS.contains(effect.type)) {
                        return@filter false
                    }

                    if (debuff) {
                        return@filter !team.isMember(it.uniqueId)
                    }

                    return@filter team.isMember(it.uniqueId)
                }.forEach{toReturn.add(it)}

                if (effect == null || !BardClass.BARD_ILLEGAL_EFFECTS.contains(effect.type)) {
                    toReturn.add(player)
                }

                return toReturn
            }

        }

    }

}