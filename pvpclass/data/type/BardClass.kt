package cc.fyre.bunkers.pvpclass.data.type

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.pvpclass.data.PvPClass
import cc.fyre.bunkers.pvpclass.data.item.energy.EnergyEffect
import cc.fyre.bunkers.pvpclass.data.item.ConsumableItem
import cc.fyre.bunkers.pvpclass.data.service.BardService
import cc.fyre.bunkers.pvpclass.event.type.BardEffectEvent
import cc.fyre.engine.util.FormatUtil
import cc.fyre.engine.util.PotionUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionEffectAddEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.function.Predicate

/**
 * @project hcf
 *
 * @date 12/09/2020
 * @author xanderume@gmail.com
 */
object BardClass : PvPClass(Type.BARD),Listener {

    val BARD_ILLEGAL_EFFECTS = arrayOf(PotionEffectType.INCREASE_DAMAGE)
    private val service = BardService(Bunkers.instance)

    init {
        this.effects.add(EnergyEffect.BardEffect(20,Material.SUGAR,PotionEffect(PotionEffectType.SPEED,6*20,1),PotionEffect(PotionEffectType.SPEED,6*20,2)))
        this.effects.add(EnergyEffect.BardEffect(45,Material.BLAZE_POWDER,PotionEffect(PotionEffectType.INCREASE_DAMAGE,6*20,0),PotionEffect(PotionEffectType.INCREASE_DAMAGE,5*20,1)))
        this.effects.add(EnergyEffect.BardEffect(40,Material.GHAST_TEAR,PotionEffect(PotionEffectType.REGENERATION,6*20,0),PotionEffect(PotionEffectType.REGENERATION,5*20,2)))
        this.effects.add(EnergyEffect.BardEffect(40,Material.IRON_INGOT,PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,6*20,0),PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,5*20,2)))
        this.effects.add(EnergyEffect.BardEffect(25,Material.FEATHER,PotionEffect(PotionEffectType.JUMP,6*20,1),PotionEffect(PotionEffectType.JUMP,5*20,6)))
        this.effects.add(EnergyEffect.BardEffect(25,Material.MAGMA_CREAM,PotionEffect(PotionEffectType.FIRE_RESISTANCE,6*20,0),PotionEffect(PotionEffectType.FIRE_RESISTANCE,46*20,0)))
        this.effects.add(EnergyEffect.BardEffect(35,Material.SPIDER_EYE,null,PotionEffect(PotionEffectType.WITHER,5*20,1)))

        this.effects.add(EnergyEffect.BardEffect(25,Material.WHEAT,null,null,Predicate{
            it.foodLevel = 20
            it.saturation = 10.0F
            return@Predicate true
        }))

        //this.effects.add(EnergyEffect.BardEffect(25,Material.FERMENTED_SPIDER_EYE,PotionEffect(PotionEffectType.INVISIBILITY,6*20,0),PotionEffect(PotionEffectType.INVISIBILITY,46*20,0)))

        this.service.runTaskTimer(Bunkers.instance,4L,4L)

        Bunkers.instance.server.pluginManager.registerEvents(this,Bunkers.instance)
    }

    override fun onEnable(player: Player) {}

    override fun onDisable(player: Player) {}

    override fun isEnergyBased(): Boolean {
        return true
    }

    override fun getConsumableItems(): List<ConsumableItem> {
        return ArrayList()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPotionEffectAdd(event: PotionEffectAddEvent) {

        if (Bunkers.instance.pvpClassHandler.findById(event.entity.uniqueId)?.type != this.type) {
            return
        }

        if (BARD_ILLEGAL_EFFECTS.none{it == event.effect.type}) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBardEffect(event: BardEffectEvent) {

        if (event.effect.effect == null) {
            return
        }

        val name = PotionUtil.getDisplayName(event.effect.effect.type)
        val level = FormatUtil.toRoman(event.effect.effect.amplifier + 1)
        val debuff = PotionUtil.isDebuff(event.effect.effect.type)

        val total = event.receivers.size

        event.player.sendMessage("${ChatColor.YELLOW}You have given $name $level ${ChatColor.YELLOW}to ${if (debuff) "${ChatColor.RED}${total}${ChatColor.YELLOW} enem${if (total == 1) "y" else "ies"}" else if (total == 0 && !BARD_ILLEGAL_EFFECTS.contains(event.effect.effect.type)) "yourself" else "${ChatColor.GREEN}$total ${ChatColor.YELLOW}teammate${if (total == 1) "" else "s"}"}.")
    }

}