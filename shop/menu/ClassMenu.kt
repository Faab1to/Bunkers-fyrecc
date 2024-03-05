package cc.fyre.bunkers.shop.menu

import cc.fyre.bunkers.shop.menu.element.BuyElement
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu
import net.frozenorb.qlib.menu.buttons.BackButton
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @project bunkers
 *
 * @date 26/12/2020
 * @author xanderume@gmail.com
 */
class ClassMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 5*9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.RED}${ChatColor.BOLD}Class"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        val toReturn = HashMap<Int,Button>()

        toReturn[10] = BuyElement(ItemStack(Material.SUGAR),10,true)
        toReturn[11] = BuyElement(ItemStack(Material.WHEAT),10,true)

        toReturn[15] = BuyElement(ItemStack(Material.BLAZE_POWDER),20,true)
        toReturn[16] = BuyElement(ItemStack(Material.GHAST_TEAR),20,true)

        toReturn[19] = BuyElement(ItemStack(Material.FEATHER),10,true)
        toReturn[20] = BuyElement(ItemStack(Material.MAGMA_CREAM),10,true)

        toReturn[24] = BuyElement(ItemStack(Material.SPIDER_EYE),20,true)
        toReturn[25] = BuyElement(ItemStack(Material.IRON_INGOT),20,true)

        toReturn[31] = BackButton(CombatMenu())

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }
}