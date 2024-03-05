package cc.fyre.bunkers.pvpclass.data

import cc.fyre.bunkers.pvpclass.data.item.energy.EnergyEffect
import cc.fyre.bunkers.pvpclass.data.item.ConsumableItem
import cc.fyre.engine.profile.data.type.BunkersProfile
import com.google.common.collect.HashBasedTable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList

/**
 * @project hcf
 *
 * @date 10/09/2020
 * @author xanderume@gmail.com
 */
abstract class PvPClass(val type: Type) {

    protected val effects = ArrayList<EnergyEffect>()

    private val cooldowns = HashBasedTable.create<UUID,Material,Long>()
    private val consumables = this.getConsumableItems()

    fun getName():String {
        return this.type.displayName
    }

    abstract fun onEnable(player: Player)
    abstract fun onDisable(player: Player)

    abstract fun isEnergyBased():Boolean
    abstract fun getConsumableItems():List<ConsumableItem>

    open fun isEquipped(player: Player):Boolean {
        return player.inventory.armorContents.all{it != null && it.type.name.startsWith(this.type.armor)}
    }

    fun getCooldown(player: Player,consumable: ConsumableItem):Long {

        if (!this.cooldowns.contains(player.uniqueId,consumable.material)) {
            return 0L
        }

        return (this.cooldowns.get(player.uniqueId,consumable.material) + consumable.cooldown) - System.currentTimeMillis()
    }

    fun setCooldown(player: Player,consumable: ConsumableItem) {
        this.cooldowns.put(player.uniqueId,consumable.material,System.currentTimeMillis())
    }

    fun findEffectByItem(material: Material):EnergyEffect? {
        return this.effects.firstOrNull{it.material == material}
    }

    fun findConsumableByItem(material: Material):ConsumableItem? {
        return this.consumables.firstOrNull{it.material == material}
    }

    enum class Type(val displayName: String,val armor: String,val effects: Array<PotionEffect>,val backendType: BunkersProfile.PvPClass) {
        DIAMOND("Diamond","DIAMOND",arrayOf(),BunkersProfile.PvPClass.DIAMOND),
        BARD("Bard","GOLD",arrayOf(PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1),PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,0),PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,1)),BunkersProfile.PvPClass.BARD);
    }

}