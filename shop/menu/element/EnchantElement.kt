package cc.fyre.bunkers.shop.menu.element

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.ShopHandler
import cc.fyre.engine.util.EnchantUtil
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.util.ItemUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class EnchantElement(private val item: ItemStack,private val enchant: Enchantment,private val level: Int,private val price: Int) : Button() {

    private var displayName: String? = null

    constructor(item: ItemStack,enchant: Enchantment,level: Int,price: Int,displayName: String):this(item,enchant,level,price) {
        this.displayName = displayName
    }

    private val name =  this.displayName ?: ItemUtils.getName(this.item)

    override fun getName(player: Player): String {

        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        return "${if (balance < this.price) ChatColor.RED else ChatColor.GREEN}Buy ${EnchantUtil.getName(this.enchant)} ${this.level}"
    }

    override fun getMaterial(p0: Player?): Material? {
        return this.item.type
    }

    override fun getDamageValue(player: Player?): Byte {
        return this.item.data.data
    }

    override fun getDescription(p0: Player?): MutableList<String> {

        val lore = arrayListOf<String>()

        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")
        lore.add("${ChatColor.GREEN}Buy ${ChatColor.YELLOW}${EnchantUtil.getName(this.enchant)} ${this.level} ${ChatColor.GREEN}for ${ChatColor.RED}$${this.price}${ChatColor.GREEN}.")
        lore.add("${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}")

        return lore
    }

    override fun clicked(player: Player,slot: Int,clickType: ClickType) {

        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        if (balance < this.price) {
            player.sendMessage("${ChatColor.RED}You cannot afford this enchant.")
            return
        }

        val item = player.inventory.armorContents.plus(player.inventory.contents).filterNotNull().filter{

            if (it.type == Material.AIR) {
                return@filter false
            }

            if (it.containsEnchantment(this.enchant)) {
                return@filter false
            }

            if (this.enchant == Enchantment.DURABILITY && this.item.type.name.contains("DIAMOND")) {
                return@filter false
            }

            if (this.enchant == Enchantment.DURABILITY) {
                return@filter ShopHandler.isArmor(it) && this.item.type.name.split("_")[1].equals(it.type.name.split("_")[1],true)
            }

            if (this.enchant == Enchantment.PROTECTION_FALL) {
                return@filter it.type.name.endsWith("BOOTS")
            }

            if (this.enchant == Enchantment.PROTECTION_ENVIRONMENTAL) {
                return@filter ShopHandler.isArmor(it) && this.item.type.name.split("_")[1].equals(it.type.name.split("_")[1],true)
            }

            // for some reason canEnchantItem returns false?
            if (this.enchant == Enchantment.ARROW_INFINITE) {
                return@filter it.type == Material.BOW
            }

            return@filter this.enchant.canEnchantItem(it)
        }.firstOrNull{ if (this.item.type == Material.FEATHER && this.enchant == Enchantment.PROTECTION_FALL) it.type.name.endsWith("BOOTS") else if (this.enchant == Enchantment.PROTECTION_ENVIRONMENTAL || this.enchant == Enchantment.DURABILITY) ShopHandler.isArmor(it) else if (this.enchant == Enchantment.ARROW_INFINITE) it.type == Material.BOW else it.type == this.item.type}

        if (item == null) {
            player.sendMessage("${ChatColor.RED}You have no item to enchant ${ChatColor.WHITE}${EnchantUtil.getName(this.enchant)}${ChatColor.RED} on.")
            return
        }

        item.addEnchantment(this.enchant,this.level)
        player.updateInventory()
        Bunkers.instance.statisticHandler.addBalance(player.uniqueId,-this.price)

        player.sendMessage("${ChatColor.GREEN}Enchanted${ChatColor.YELLOW} ${EnchantUtil.getName(this.enchant)} ${this.level} ${ChatColor.GREEN}on your ${ChatColor.WHITE}${ItemUtils.getName(ItemStack(item.type))} ${ChatColor.GREEN}for ${ChatColor.WHITE}$${this.price}${ChatColor.GREEN}.")

    }


}