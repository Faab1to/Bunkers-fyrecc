package cc.fyre.bunkers.shop.menu

import cc.fyre.bunkers.shop.menu.element.SellElement


import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 04/08/2020
 * @author xanderume@gmail.com
 */
class SellMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.GREEN}${ChatColor.BOLD}Sell"
    }

    override fun getButtons(player: Player): MutableMap<Int,Button> {

        val toReturn = HashMap<Int,Button>()

        toReturn[2] = SellElement(Material.EMERALD,40,true)
        toReturn[3] = SellElement(Material.DIAMOND,30,true)
        toReturn[4] = SellElement(Material.GOLD_INGOT,25,true)
        toReturn[5] = SellElement(Material.IRON_INGOT,20,true)
        toReturn[6] = SellElement(Material.COAL,10,true)

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

}