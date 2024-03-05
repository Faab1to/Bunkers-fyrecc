package cc.fyre.bunkers.shop.menu.element

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.ShopHandler
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
class BuyElement(private val item: ItemStack,private val price: Int,private val fill: Boolean) : Button() {

    private var name: String? = null
    private var displayName: String? = null

    constructor(item: ItemStack,displayName: String,price: Int,fill: Boolean):this(item,price,fill) {
        this.name = displayName
        this.displayName = displayName
    }

    override fun getName(player: Player): String {

        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        if (this.name == null) {
            this.name = ItemUtils.getName(ItemStack(this.item.type))
        }

        return "${if (balance < this.price) ChatColor.RED else ChatColor.GREEN}Buy ${this.name}"
    }

    override fun getMaterial(p0: Player?): Material? {
        return this.item.type
    }

    override fun getDamageValue(player: Player?): Byte {
        return this.item.data.data
    }

    override fun getDescription(player: Player): MutableList<String> {

        val lore = ArrayList<String>()
        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")
        lore.add("${ChatColor.GREEN}Buy ${ChatColor.YELLOW}1x ${this.name}${ChatColor.GREEN} for ${ChatColor.RED}$${this.price}${ChatColor.GREEN}.")

        if (this.fill) {

            var amount = if (this.item.maxStackSize != 1) this.item.maxStackSize else ShopHandler.findAvailableSlots(player.inventory)

            if (amount * this.price > balance) {
                amount = (balance / this.price)
            }

            if (amount != 0) {
                lore.add("${ChatColor.GREEN}Buy ${ChatColor.YELLOW}${amount}x ${this.name}${ChatColor.GREEN} for ${ChatColor.RED}$${amount * this.price}${ChatColor.GREEN}.")
            }

        }

        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")

        return lore
    }

    //TODO: cleanup armor part

    override fun clicked(player: Player,slot: Int,clickType: ClickType) {

        if (this.name == null) {
            this.name = ItemUtils.getName(ItemStack(this.item.type))
        }

        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        if (!this.fill || clickType.isLeftClick) {

            if (balance < this.price) {
                player.sendMessage("${ChatColor.RED}You cannot afford this item.")
                return
            }

            if (ShopHandler.isArmor(this.item)) {

                val part = this.item.type.name.toUpperCase().split("_")[1]

                when {
                    part == "HELMET" && (player.inventory.helmet == null || player.inventory.helmet.type == Material.AIR) -> player.inventory.helmet = this.item
                    part == "CHESTPLATE" && (player.inventory.chestplate == null || player.inventory.chestplate.type == Material.AIR) -> player.inventory.chestplate = this.item
                    part == "LEGGINGS" && (player.inventory.leggings == null || player.inventory.leggings.type == Material.AIR) -> player.inventory.leggings = this.item
                    part == "BOOTS" && (player.inventory.boots == null || player.inventory.boots.type == Material.AIR) -> player.inventory.boots = this.item
                    else -> player.inventory.addItem(this.item)
                }

                player.updateInventory()
            } else {
                player.inventory.addItem(this.item)
            }

            player.sendMessage("${ChatColor.GREEN}Purchased ${ChatColor.YELLOW}1${ChatColor.GREEN} ${this.name} for ${ChatColor.WHITE}$${this.price}${ChatColor.GREEN}.")

            Bunkers.instance.statisticHandler.addBalance(player.uniqueId,-this.price)
            return
        }

        if (!clickType.isRightClick) {
            return
        }

        var amount = if (this.item.maxStackSize != 1) this.item.maxStackSize else ShopHandler.findAvailableSlots(player.inventory)

        if (amount * this.price > balance) {
            amount = (balance / this.price)
        }

        if (amount == 0) {
            return
        }

        val item = this.item.clone()

        item.amount = amount

        if (this.item.maxStackSize > 1) {
            player.inventory.addItem(item)
        } else {
            repeat(amount) {

                val new = item.clone()

                new.amount = 1

                player.inventory.addItem(new)
            }

        }

        player.sendMessage("${ChatColor.GREEN}Purchased ${ChatColor.YELLOW}${amount}${ChatColor.GREEN} ${"${this.name}${if (amount > 1) "" else "s"}"} for ${ChatColor.WHITE}$${amount * this.price}${ChatColor.GREEN}.")

        Bunkers.instance.statisticHandler.addBalance(player.uniqueId,-(amount*this.price))
    }

}