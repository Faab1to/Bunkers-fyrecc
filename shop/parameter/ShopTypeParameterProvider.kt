package cc.fyre.bunkers.shop.parameter

import cc.fyre.bunkers.shop.data.ShopType
import net.frozenorb.qlib.command.ParameterType
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 04/08/2020
 * @author xanderume@gmail.com
 */
class ShopTypeParameterProvider : ParameterType<ShopType?> {

    override fun transform(sender: CommandSender, source: String): ShopType? {

        val toReturn = ShopType.values().firstOrNull{it.name.equals(source,true)}

        if (toReturn == null) {
            sender.sendMessage("${ChatColor.RED}Shop type ${ChatColor.YELLOW}$source${ChatColor.RED} not found.")
            return null
        }

        return toReturn
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        return emptyList()
    }

}