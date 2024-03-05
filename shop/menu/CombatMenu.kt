package cc.fyre.bunkers.shop.menu

import cc.fyre.bunkers.game.BunkersGameAdapter
import cc.fyre.bunkers.shop.menu.element.BuyElement

import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu

import net.frozenorb.qlib.util.ItemBuilder
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
class CombatMenu : Menu() {

    override fun size(buttons: Map<Int,Button>): Int {
        return 6*9
    }

    override fun getTitle(player: Player): String {
        return "${ChatColor.RED}${ChatColor.BOLD}Combat"
    }

    override fun getButtons(player: Player): MutableMap<Int,Button> {

        val toReturn = HashMap<Int,Button>()

        toReturn[9] = BuyElement(ItemStack(Material.DIAMOND_HELMET),75,false)
        toReturn[18] = BuyElement(ItemStack(Material.DIAMOND_CHESTPLATE),200,false)
        toReturn[27] = BuyElement(ItemStack(Material.DIAMOND_LEGGINGS),150,false)
        toReturn[36] = BuyElement(ItemStack(Material.DIAMOND_BOOTS),75,false)

        toReturn[19] = BuyElement(ItemStack(Material.DIAMOND_SWORD),100,false)
        toReturn[28] = BuyElement(ItemStack(Material.ENDER_PEARL),25,true)

        toReturn[13] = object : Button() {

            override fun getName(p0: Player?): String {
                return "${ChatColor.RED}${ChatColor.BOLD}Class Shop"
            }

            override fun getDescription(p0: Player?): MutableList<String> {
                return arrayListOf()
            }

            override fun getMaterial(p0: Player?): Material {
                return Material.GOLD_INGOT
            }

            override fun clicked(player: Player?, slot: Int, clickType: ClickType?) {
                ClassMenu().openMenu(player)
            }

        }

        toReturn[21] = BuyElement(ItemBuilder.of(Material.POTION).data(8226).build(),"Speed Potion",10,false)
        toReturn[22] = BuyElement(BunkersGameAdapter.ANTIDOTE,"Antidote",150,false)
        toReturn[23] = BuyElement(BunkersGameAdapter.LESSER_INVISIBILITY,"Lesser Invisibility",300,false)
        toReturn[30] = BuyElement(ItemBuilder.of(Material.POTION).data(16421).build(),"Healing Potion",5,true)
        toReturn[31] = BuyElement(ItemBuilder.of(Material.POTION).data(16388).build(),"Poison Potion",50,false)
        toReturn[32] = BuyElement(ItemBuilder.of(Material.POTION).data(16426).build(),"Slowness Potion",50,false)
        toReturn[40] = BuyElement(ItemBuilder.of(Material.POTION).data(16430).build(),"Invisibility Potion",1250,false)

        toReturn[17] = BuyElement(ItemStack(Material.GOLD_HELMET),75,false)
        toReturn[26] = BuyElement(ItemStack(Material.GOLD_CHESTPLATE),200,false)
        toReturn[35] = BuyElement(ItemStack(Material.GOLD_LEGGINGS),150,false)
        toReturn[44] = BuyElement(ItemStack(Material.GOLD_BOOTS),75,false)

        return toReturn
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

}
