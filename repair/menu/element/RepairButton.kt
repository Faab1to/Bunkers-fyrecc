package cc.fyre.bunkers.repair.menu.element

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.util.ItemBuilder
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
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class RepairButton(private val itemStack: ItemStack?) : Button() {

    private val name =  if (this.itemStack == null || this.itemStack.type == Material.AIR) "Air" else ItemUtils.getName(ItemStack(this.itemStack.type))

    override fun getName(p0: Player?): String ?{
        return null
    }

    override fun getMaterial(p0: Player?): Material? {
        return null
    }

    override fun getDescription(p0: Player?): MutableList<String>? {
        return null
    }

    override fun getButtonItem(player: Player?): ItemStack {

        if (this.itemStack == null || this.itemStack.type == Material.AIR || this.itemStack.type.maxDurability == 0.toShort()) {
            return ItemBuilder.of(Material.STAINED_GLASS_PANE)
                .name(" ")
                .data(15)
                .build()
        }

        if (this.itemStack.durability == 0.toShort()) {
            return ItemBuilder.of(Material.STAINED_GLASS_PANE).name(" ").data(13).build()
        }

        val price = Bunkers.instance.repairHandler.calculatePrice(this.itemStack)

        return ItemBuilder.copyOf(this.itemStack.clone()).name("${ChatColor.GOLD}${this.name}").addToLore(
            "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}",
            "${ChatColor.GREEN}Repair ${ChatColor.YELLOW}${this.name}${ChatColor.GREEN} for ${ChatColor.RED}$${price}${ChatColor.GREEN}.",
            "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",18)}"
        ).build()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType?) {

        if (this.itemStack == null || this.itemStack.type == Material.AIR) {
            return
        }

        if (this.itemStack.durability == 0.toShort()) {
            return
        }

        val price = Bunkers.instance.repairHandler.calculatePrice(this.itemStack)

        if (price > Bunkers.instance.statisticHandler.getBalance(player.uniqueId)) {
            player.sendMessage("${ChatColor.RED}You cannot afford to repair this item.")
            return
        }

        this.itemStack.durability = 0

        Bunkers.instance.statisticHandler.addBalance(player.uniqueId,-price)
    }

}