package cc.fyre.bunkers.villager.data

import net.minecraft.server.v1_7_R4.DamageSource
import net.minecraft.server.v1_7_R4.Entity
import net.minecraft.server.v1_7_R4.EntityAgeable
import net.minecraft.server.v1_7_R4.EntityVillager
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld

/**
 * @project bunkers
 *
 * @date 16/08/2020
 * @author xanderume@gmail.com
 */
class VillagerEntity(location: Location):EntityVillager((location.world as CraftWorld).handle) {

    override fun move(x: Double,y: Double,z: Double) {}
    override fun collide(entity: Entity?) {}
    override fun g(d0: Double, d1: Double, d2: Double) {}

    override fun dropDeathLoot(flag: Boolean, i: Int) {}
    override fun createChild(entity: EntityAgeable?): EntityAgeable? = null

    override fun damageEntity(damagesource: DamageSource,float: Float): Boolean {
        return super.damageEntity(damagesource,float / 2.5F)
    }

}