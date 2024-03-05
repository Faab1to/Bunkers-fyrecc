package cc.fyre.bunkers.pvpclass.data.service

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.pvpclass.PvPClassHandler
import cc.fyre.bunkers.pvpclass.data.PvPClass
import cc.fyre.bunkers.pvpclass.data.item.energy.EnergyEffect
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project hcf
 *
 * @date 12/09/2020
 * @author xanderume@gmail.com
 */
class BardService(private val instance: Bunkers) : BukkitRunnable() {

    override fun run() {

        for (entry in this.instance.pvpClassHandler.cache.filterValues{it == PvPClass.Type.BARD}.mapKeys{this.instance.server.getPlayer(it.key)}.mapValues{this.instance.pvpClassHandler.findByType(it.value)}) {

            if (entry.key == null || !entry.key.isOnline) {
                continue
            }

            if (entry.value == null) {
                continue
            }

            if (entry.key.itemInHand == null) {
                continue
            }

            val effect = entry.value!!.findEffectByItem(entry.key.itemInHand.type)

            if (effect == null || effect !is EnergyEffect.BardEffect || effect.passive == null) {
                continue
            }

            val team = this.instance.teamHandler.findById(entry.key.uniqueId) ?: return

            val receivers = EnergyEffect.BardEffect.findReceivers(entry.key,team,PvPClassHandler.BARD_RANGE,EnergyEffect.BardEffect.isDebuff(effect.passive.type),effect.passive)

            receivers.forEach{this.instance.pvpClassHandler.addPotionEffect(it,effect.passive)}
        }

    }

}