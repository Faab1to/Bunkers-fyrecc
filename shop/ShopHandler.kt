package cc.fyre.bunkers.shop

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.data.ShopType
import cc.fyre.bunkers.shop.listener.ShopListener
import cc.fyre.bunkers.shop.parameter.ShopTypeParameterProvider
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.villager.data.VillagerEntity
import net.frozenorb.qlib.command.FrozenCommandHandler
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
class ShopHandler(private val instance: Bunkers) {

    init {
        this.instance.server.pluginManager.registerEvents(ShopListener(this.instance),this.instance)

        FrozenCommandHandler.registerParameterType(ShopType::class.java,ShopTypeParameterProvider())
    }

    fun spawnVillager(location: Location,shop: ShopType,team: Team.Type):VillagerEntity {

        val villager = this.instance.villagerHandler.spawnVillager(location)

        villager.bukkitEntity.setMetadata(TYPE_METADATA,FixedMetadataValue(this.instance,shop.name))
        villager.bukkitEntity.setMetadata(TEAM_METADATA,FixedMetadataValue(this.instance,team.name))

        villager.customName = "${team.color}${shop.getDisplayName()}"
        villager.customNameVisible = true

        return villager
    }

    companion object {

        const val TYPE_METADATA = "SHOP_TYPE"
        const val TEAM_METADATA = "TEAM_TYPE"

        const val RESPAWN_SECONDS = 300

        fun isArmor(item: ItemStack):Boolean {
            return item.type.name.endsWith("HELMET",true)
                    || item.type.name.endsWith("CHESTPLATE",true)
                    || item.type.name.endsWith("LEGGINGS",true)
                    || item.type.name.endsWith("BOOTS",true)
        }

        fun findAvailableSlots(inventory: Inventory):Int {
            return inventory.contents.filter{it == null || it.type == Material.AIR}.count()
        }

    }

}