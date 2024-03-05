package cc.fyre.bunkers.shop.menu

import cc.fyre.bunkers.shop.menu.element.BuyElement
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @project bunkers
 *
 * @date 05/08/2020
 * @author xanderume@gmail.com
 */
class BuildMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 5*9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.BLUE}${ChatColor.BOLD}Build"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        val toReturn = HashMap<Int,Button>()

        toReturn[10] = BuyElement(ItemStack(Material.FENCE_GATE),3,true)
        toReturn[11] = BuyElement(ItemStack(Material.LADDER),3,true)
        toReturn[12] = BuyElement(ItemStack(Material.CHEST),3,true)

        toReturn[14] = BuyElement(ItemStack(Material.COBBLESTONE),1,true)
        toReturn[15] = BuyElement(ItemStack(Material.STONE),1,true)
        toReturn[16] = BuyElement(ItemStack(Material.SMOOTH_BRICK),1,true)

        toReturn[30] = BuyElement(ItemStack(Material.DIAMOND_PICKAXE),50,false)
        toReturn[31] = BuyElement(ItemStack(Material.DIAMOND_AXE),50,false)
        toReturn[32] = BuyElement(ItemStack(Material.DIAMOND_SPADE),50,false)

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

}