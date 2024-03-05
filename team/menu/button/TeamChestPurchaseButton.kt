package cc.fyre.bunkers.team.menu.button

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.data.TeamChest
import cc.fyre.bunkers.team.menu.TeamChestModifyMenu
import net.frozenorb.qlib.menu.Button
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class TeamChestPurchaseButton(private val double: Boolean, private val location: Location, private val value: Boolean) : Button() {

    override fun getName(p0: Player?): String {

        if (this.value) {
            return "${ChatColor.GREEN}${ChatColor.BOLD}Purchase Chest"
        }

        return "${ChatColor.RED}${ChatColor.BOLD}Cancel Purchase"
    }

    override fun getMaterial(p0: Player?): Material {
        return Material.WOOL
    }

    override fun getDescription(p0: Player?): MutableList<String> {
        return mutableListOf()
    }

    override fun getDamageValue(player: Player?): Byte {
        return if (this.value) DyeColor.LIME.woolData else DyeColor.RED.woolData
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType?) {

        if (!this.value) {
            player.sendMessage("${ChatColor.RED}Purchase cancelled.")
            return
        }

        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)

        if (team == null) {
            player.sendMessage("${ChatColor.RED}You are not on a team!")
            return
        }


        val balance = Bunkers.instance.statisticHandler.getBalance(player.uniqueId)

        if (balance <= TeamHandler.CHEST_PRICE) {
            player.closeInventory()
            player.sendMessage("${ChatColor.RED}You need ${ChatColor.BOLD}$${TeamHandler.CHEST_PRICE}${ChatColor.RED} to purchase this chest.")
            return
        }

        val chest = TeamChest(player.uniqueId,this.double)

        team.chests[this.location] = chest

        player.sendMessage("${ChatColor.GREEN}You have purchased this chest for ${ChatColor.BOLD}${TeamHandler.CHEST_PRICE}${ChatColor.GREEN}!")

        TeamChestModifyMenu(chest).openMenu(player)
    }

}