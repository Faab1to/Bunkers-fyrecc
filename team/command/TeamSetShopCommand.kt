package cc.fyre.bunkers.team.command

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.data.ShopType
import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import cc.fyre.engine.map.data.Map
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * @project bunkers
 *
 * @date 04/08/2020
 * @author xanderume@gmail.com
 */
object TeamSetShopCommand {

    @JvmStatic
    @Command(names = ["team setshop","t setshop","faction setshop","fac setshop","f setshop"],async = true,permission = "bunkers.command.team.setshop")
    fun execute(player: Player, @Param(name = "shop")type: ShopType, @Param(name = "team")team: Team, @Param(name = "map",defaultValue = "current")map: Map) {
        team.shops[type] = Location(Bunkers.instance.server.getWorld(map.id),player.location.x,player.location.y,player.location.z,player.location.yaw,player.location.pitch)

        if (!Bunkers.instance.teamHandler.saveTeamData(team,map).wasAcknowledged()) {
            player.sendMessage("${ChatColor.RED}Failed to update team data..")
            return
        }

        player.sendMessage("${ChatColor.YELLOW}Updated ${team.getDisplayName()}${ChatColor.YELLOW}'s ${type.name} shop on map ${ChatColor.LIGHT_PURPLE}${map.id}${ChatColor.YELLOW}.")
    }

}