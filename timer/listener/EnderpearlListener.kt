package cc.fyre.bunkers.timer.listener

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.timer.data.TimerType
import cc.fyre.engine.util.FormatUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerPearlRefundEvent
import org.bukkit.inventory.ItemStack

/**
 * @project hcf
 *
 * @date 20/07/2020
 * @author xanderume@gmail.com
 */
class EnderpearlListener(private val instance: Bunkers):Listener {

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    private fun onProjectileLaunched(event: ProjectileLaunchEvent) {

        if (event.entity !is EnderPearl || event.entity.shooter !is Player) {
            return
        }

        this.instance.timerHandler.addTimer((event.entity.shooter as Player).uniqueId,TimerType.ENDER_PEARL)
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    private fun onProjectileLaunch(event: ProjectileLaunchEvent) {

        if (event.entity !is EnderPearl || event.entity.shooter !is Player) {
            return
        }

        val cooldown = this.instance.timerHandler.findRemaining((event.entity.shooter as Player).uniqueId,TimerType.ENDER_PEARL)

        if (cooldown <= 0) {
            return
        }

        event.isCancelled = true

        (event.entity.shooter as Player).sendMessage("${ChatColor.RED}You cannot use this for another ${ChatColor.BOLD}${FormatUtil.formatIntoFancy(cooldown)}${ChatColor.RED}.")
        (event.entity.shooter as Player).updateInventory()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPearlRefund(event: PlayerPearlRefundEvent) {
        event.player.inventory.addItem(ItemStack(Material.ENDER_PEARL))
        this.instance.timerHandler.removeTimer(event.player.uniqueId,TimerType.ENDER_PEARL)
    }

//    @EventHandler(priority = EventPriority.LOW)
//    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
//
//        if (event.isCancelled || event.cause != PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.to.world.environment != World.Environment.NORMAL) {
//            return
//        }
//
//        val face = this.getDirection(event.player)
//        val block = this.getDirectionalBlock(event.to.block,face)
//
//        if ((block == null || block.type != Material.FENCE_GATE || block.getRelative(BlockFace.UP).type != Material.TRAP_DOOR) && event.to.block.type != Material.FENCE_GATE && EntityEnderPearl.pearlAbleType.stream().noneMatch { it: String? -> event.to.block.type.name.contains(it!!) } && !event.to.block.getRelative(BlockFace.UP).type.name.contains("STEP") || event.to.block.type == null || block == null && EntityEnderPearl.pearlAbleType.none{event.to.block.type.name.contains(it)}) {
//            return
//        }
//
//        if (this.isPearlGlitching(block) && event.to.block.type != Material.AIR) {
//            event.isCancelled = true
//            this.instance.server.pluginManager.callEvent(PlayerPearlRefundEvent(event.player))
//            return
//        }
//
//        event.to = this.findSuitableLocation(event.to,face)
//    }

//    private fun getDirectionalBlock(block: Block,face: BlockFace): Block? {
//
//        val relative = block.getRelative(face)
//
//        if (relative == null || relative.type == Material.AIR) {
//            return null
//        }
//
//        return relative
//    }

//    private fun isPearlGlitching(block: Block?): Boolean {
//        return block != null && block.type != Material.AIR && block.type.isSolid && !RETURN_TYPES.contains(block.type) || block is Openable && !(block as Openable).isOpen
//    }

//    private fun findSuitableLocation(location: Location,face: BlockFace): Location {
//
//        val toReturn = location.clone()
//
//        if (face.ordinal <= 3) {
//            toReturn.x += face.modX
//            toReturn.y += face.modY
//            toReturn.z += face.modZ
//        }
//
//        if (toReturn.block.getRelative(BlockFace.DOWN) == null || !toReturn.block.getRelative(BlockFace.DOWN).type.isSolid) {
//            toReturn.y += BlockFace.DOWN.modY
//        }
//
//        return toReturn
//    }

//    private fun getDirection(player: Player): BlockFace {
//
//        var toReturn = player.location.yaw
//
//        if (toReturn < 0) {
//            toReturn += 360.0F
//        }
//
//        if (toReturn >= 315 || toReturn < 45) {
//            return BlockFace.SOUTH
//        } else if (toReturn < 135) {
//            return BlockFace.WEST
//        } else if (toReturn < 225) {
//            return BlockFace.NORTH
//        } else if (toReturn < 315) {
//            return BlockFace.EAST
//        }
//
//        return BlockFace.NORTH
//    }

    companion object {

        val RETURN_TYPES = arrayListOf(
                Material.LADDER,Material.BRICK_STAIRS,Material.SMOOTH_STAIRS,Material.WOOD_STAIRS,Material.SPRUCE_WOOD_STAIRS,Material.TORCH,Material.NETHER_BRICK_STAIRS,
                Material.QUARTZ_STAIRS,Material.BEDROCK,Material.CAKE,Material.STEP,Material.WOOD_STEP,Material.LEVER,Material.DAYLIGHT_DETECTOR,Material.COBBLE_WALL,
                Material.FENCE_GATE,Material.SIGN,Material.SIGN_POST,Material.WALL_SIGN,Material.STATIONARY_WATER,Material.WATER,Material.LAVA,Material.STATIONARY_LAVA,
                Material.STONE_PLATE,Material.WOOD_PLATE,Material.IRON_PLATE,Material.GOLD_PLATE
        )

    }
}