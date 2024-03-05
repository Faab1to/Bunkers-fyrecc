package cc.fyre.bunkers.villager

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.villager.data.VillagerEntity
import cc.fyre.bunkers.villager.listener.VillagerPacketListener
import cc.fyre.bunkers.villager.data.kb.ZeroKBProfile
import cc.fyre.bunkers.villager.listener.VillagerListener
import cc.fyre.bunkers.villager.listener.VillagerLoggerListener
import cc.fyre.bunkers.villager.service.VillagerLoggerService
import com.comphenix.protocol.ProtocolLibrary
import net.frozenorb.qlib.qLib
import net.frozenorb.qlib.serialization.ItemStackAdapter
import net.minecraft.server.v1_7_R4.EntityTypes
import org.bukkit.ChatColor

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * @project bunkers
 *
 * @date 03/08/2020
 * @author xanderume@gmail.com
 */
class VillagerHandler(private val instance: Bunkers) {

	val cache = HashSet<VillagerEntity>()
	val items = HashMap<UUID,Array<ItemStack>>()
	val loggers = HashMap<UUID,VillagerEntity>()
	val services = HashMap<UUID,VillagerLoggerService>()

    init {
		this.registerEntity(VillagerEntity::class.java,"Villager",120)

		this.instance.server.pluginManager.registerEvents(VillagerListener(this.instance),this.instance)
		this.instance.server.pluginManager.registerEvents(VillagerLoggerListener(this.instance),this.instance)

		ProtocolLibrary.getProtocolManager().addPacketListener(VillagerPacketListener(this.instance))
	}

	fun spawnVillager(location: Location):VillagerEntity {

		if (!location.chunk.isLoaded) {
			location.chunk.load()
		}

		val villager = VillagerEntity(location)

		villager.setLocation(location.x,location.y,location.z,(location.yaw * 256.0f / 360.0f),(location.pitch * 256.0f / 360.0f))
		villager.setPosition(location.x,location.y,location.z)

		val result = villager.world.addEntity(villager)

		if (!result) {
			this.instance.server.logger.info("Failed to spawn villager.")
		}

		villager.k = false
		villager.fromMobSpawner = true

		villager.health = villager.maxHealth
		villager.kbProfile = KB_PROFILE

		this.cache.add(villager)

		return villager
	}

	fun spawnCombatLogger(location: Location,player: Player) {

		val villager = this.spawnVillager(location)

		villager.customName = "${ChatColor.GRAY}(Combat-Logger)${this.instance.teamHandler.findById(player.uniqueId)?.getColor() ?: ChatColor.WHITE} ${player.name}"
		villager.customNameVisible = true

		val items = ArrayList<ItemStack>()

		items.addAll(player.inventory.contents.filterNotNull().filter{it.type != Material.AIR})
		items.addAll(player.inventory.armorContents.filterNotNull().filter{it.type != Material.AIR})

		villager.bukkitEntity.setMetadata(LOGGER_OWNER_METADATA,FixedMetadataValue(this.instance,player.uniqueId.toString()))
		villager.bukkitEntity.setMetadata(LOGGER_ITEMS_METADATA,FixedMetadataValue(this.instance,qLib.GSON.toJson(items,ItemStackAdapter::class.java)))

		this.items[player.uniqueId] = player.inventory.contents.plus(player.inventory.armorContents).filterNotNull().filter{it.type != Material.AIR}.toTypedArray()
		this.loggers[player.uniqueId] = villager

		val service = VillagerLoggerService(player.uniqueId,villager)

		service.runTaskTimer(this.instance,20L,20L)

		this.services[player.uniqueId] = service
	}

	private fun registerEntity(entityClass: Class<*>, name: String, id: Int) {
		this.setFieldPrivateStaticMap("d",entityClass,name)
		this.setFieldPrivateStaticMap("f",entityClass,Integer.valueOf(id))
	}

	private fun setFieldPrivateStaticMap(fieldName: String, key: Any, value: Any) {

		try {
			val field = EntityTypes::class.java.getDeclaredField(fieldName)
			field.isAccessible = true
			val map = field.get(null) as HashMap<Any,Any>
			map[key] = value
			field.set(null, map)
		} catch (ex: SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		} catch (ex: IllegalAccessException) {
			ex.printStackTrace()
		} catch (ex: NoSuchFieldException) {
			ex.printStackTrace()
		}

	}

	companion object {

		val KB_PROFILE = ZeroKBProfile()

		const val LOGGER_OWNER_METADATA = "LOGGER_OWNER"
		const val LOGGER_ITEMS_METADATA = "LOGGER_ITEMS"

	}

}