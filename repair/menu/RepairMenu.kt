package cc.fyre.bunkers.repair.menu

import cc.fyre.bunkers.repair.menu.element.RepairButton
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class RepairMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 6*9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Repair"
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        val toReturn = HashMap<Int,Button>()

        toReturn[11] = RepairButton(player.inventory.helmet)
        toReturn[20] = RepairButton(player.inventory.chestplate)
        toReturn[29] = RepairButton(player.inventory.leggings)
        toReturn[38] = RepairButton(player.inventory.boots)
        toReturn[42] = RepairButton(player.inventory.itemInHand)

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

}