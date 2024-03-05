package cc.fyre.bunkers.shop.menu.element

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.util.ItemUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * @project bunkers
 *
 * @date 05/08/2020
 * @author xanderume@gmail.com
 */
class SellElement(private val item: Material,private val price: Int,private val all: Boolean) : Button() {

    private val name = ItemUtils.getName(ItemStack(this.item)).replace(" Ingot","")

    override fun getName(player: Player): String {
        return "${if (player.inventory.contents.any{it != null && it.type == this.item}) ChatColor.RED else ChatColor.GREEN}Sell ${this.name}"
    }

    override fun getMaterial(p0: Player?): Material {
        return this.item
    }

    override fun getDescription(p0: Player?): MutableList<String> {
        val lore = ArrayList<String>()

        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")
        lore.add("${ChatColor.GREEN}Sell ${ChatColor.YELLOW}1x ${this.name}${ChatColor.GREEN} for ${ChatColor.RED}$${this.price}${ChatColor.GREEN}.")
        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")

        return lore
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType) {

        val collections = player.inventory.contents.filter{it != null && it.type == this.item}.toCollection(ArrayList())

        if (collections.isEmpty()) {
            player.sendMessage("${ChatColor.RED}You do not have any ${this.name.toLowerCase()} to sell.")
            return
        }

        if (!this.all || clickType.isLeftClick) {
            val item = player.inventory.contents.firstOrNull{it != null && it.type == this.item}

            if (item == null) {
                player.sendMessage("${ChatColor.RED}You do not have any ${this.name.toLowerCase()} to sell.")
                return
            }

            if (item.amount == 1) {
                player.inventory.removeItem(item)
            } else {
                item.amount = item.amount - 1
            }

            player.updateInventory()
            player.sendMessage("${ChatColor.GREEN}Sold ${ChatColor.YELLOW}1 ${ChatColor.GREEN}${this.name} for ${ChatColor.WHITE}$${this.price}${ChatColor.YELLOW}.")

            Bunkers.instance.statisticHandler.addBalance(player.uniqueId,this.price)
            return
        }

        val amount = collections.filter{it != null && it.type == this.item}.map{it.amount}.sum()

        collections.forEach{player.inventory.removeItem(it)}

        player.updateInventory()
        player.sendMessage("${ChatColor.GREEN}Sold ${ChatColor.YELLOW}${amount} ${ChatColor.GREEN}${"${this.name}${if (this.isPlural()) "s" else ""}"} for ${ChatColor.WHITE}$${amount * this.price}${ChatColor.YELLOW}.")

        Bunkers.instance.statisticHandler.addBalance(player.uniqueId,this.price * amount)
    }

    private fun isPlural():Boolean {
        return this.item != Material.IRON_INGOT && this.item != Material.GOLD_INGOT && this.item != Material.COAL
    }

}