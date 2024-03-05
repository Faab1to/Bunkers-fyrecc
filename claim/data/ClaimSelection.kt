package cc.fyre.bunkers.claim.data

import cc.fyre.bunkers.team.data.Team
import cc.fyre.engine.map.data.Map
import net.frozenorb.qlib.util.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material

import org.bukkit.event.block.Action

/**
 * @project hcf
 *
 * @date 04/04/2020
 * @author xanderume@gmail.com
 */
class ClaimSelection(val map: Map,val team: Team) {

    var first: Location? = null
    var second: Location? = null

    fun getFormattedMessage(action: Action,location: Location):Array<String?> {

        val toReturn = arrayOfNulls<String>(1)

        toReturn[0] = "${ChatColor.YELLOW}Set claim's location ${ChatColor.LIGHT_PURPLE}${if (action == Action.LEFT_CLICK_BLOCK) 1 else 2}${ChatColor.YELLOW} to ${ChatColor.GREEN}(${ChatColor.WHITE}${location.blockX}, ${location.blockY}, ${location.blockZ}${ChatColor.GREEN})${ChatColor.YELLOW}."

        return toReturn
    }

    companion object {

        val ITEM = ItemBuilder.of(Material.WOOD_HOE)
            .name("${ChatColor.GREEN}${ChatColor.ITALIC}Claiming Wand")
            .addToLore(
                "",
                "${ChatColor.YELLOW}Right Click ${ChatColor.GOLD}Air",
                "${ChatColor.AQUA}- ${ChatColor.WHITE}Cancel current claim",
                "",
                "${ChatColor.YELLOW}Right/Left Click ${ChatColor.GOLD}Block",
                "${ChatColor.AQUA}- ${ChatColor.WHITE}Select claim's corners",
                "",
                "${ChatColor.BLUE}Crouch ${ChatColor.YELLOW}Left Click ${ChatColor.GOLD}Block/Air",
                "${ChatColor.AQUA}- ${ChatColor.WHITE}Purchase current claim"
            ).build()
    }

}