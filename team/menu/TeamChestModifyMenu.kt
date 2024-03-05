package cc.fyre.bunkers.team.menu

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.TeamChest
import cc.fyre.bunkers.team.menu.button.TeamChestMemberButton
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu
import org.bukkit.entity.Player

class TeamChestModifyMenu(private val chest: TeamChest) : Menu() {

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 3*9
    }

    override fun getTitle(player: Player): String {
        return "${if (this.chest.double) "Large " else ""}Chest"
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        var index = 12
        val team = Bunkers.instance.teamHandler.findById(player.uniqueId)!!
        val toReturn = mutableMapOf<Int,Button>()

        for (member in team.members.filter{it != player.uniqueId}) {
            toReturn[index++] = TeamChestMemberButton(this.chest,member)
        }

        return toReturn
    }

}