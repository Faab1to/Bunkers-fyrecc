package cc.fyre.bunkers.shop.menu

import cc.fyre.bunkers.shop.menu.element.EnchantElement

import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class EnchantMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 6*9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.BLUE}${ChatColor.BOLD}Enchant"
    }

    override fun getButtons(player: Player): MutableMap<Int,Button> {

        val toReturn = HashMap<Int, Button>()

        toReturn[10] = EnchantElement(ItemStack(Material.DIAMOND_HELMET),Enchantment.PROTECTION_ENVIRONMENTAL,1,300)
        toReturn[19] = EnchantElement(ItemStack(Material.DIAMOND_CHESTPLATE),Enchantment.PROTECTION_ENVIRONMENTAL,1,300)
        toReturn[28] = EnchantElement(ItemStack(Material.DIAMOND_LEGGINGS),Enchantment.PROTECTION_ENVIRONMENTAL,1,300)
        toReturn[37] = EnchantElement(ItemStack(Material.DIAMOND_BOOTS),Enchantment.PROTECTION_ENVIRONMENTAL,1,300)

        toReturn[12] = EnchantElement(ItemStack(Material.DIAMOND_SWORD),Enchantment.DAMAGE_ALL,1,125)
        toReturn[13] = EnchantElement(ItemStack(Material.FEATHER),Enchantment.PROTECTION_FALL,4,125)
        toReturn[14] = EnchantElement(ItemStack(Material.DIAMOND_PICKAXE),Enchantment.DIG_SPEED,3,500)

        toReturn[16] = EnchantElement(ItemStack(Material.GOLD_HELMET),Enchantment.DURABILITY,3,350)
        toReturn[25] = EnchantElement(ItemStack(Material.GOLD_CHESTPLATE),Enchantment.DURABILITY,3,350)
        toReturn[34] = EnchantElement(ItemStack(Material.GOLD_LEGGINGS),Enchantment.DURABILITY,3,350)
        toReturn[43] = EnchantElement(ItemStack(Material.GOLD_BOOTS),Enchantment.DURABILITY,3,350)

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }
}