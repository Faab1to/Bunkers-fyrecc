package cc.fyre.bunkers.team.menu

import cc.fyre.bunkers.team.menu.button.TeamChestPurchaseButton
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.menu.Menu
import org.bukkit.Location
import org.bukkit.entity.Player

class TeamChestPurchaseMenu(private val double: Boolean, private val location: Location) : Menu() {

    override fun size(buttons: MutableMap<Int, Button>?): Int {
        return 3*9
    }

    override fun getTitle(player: Player): String {
        return "Buy Chest"
    }

    override fun isPlaceholder(): Boolean {
        return true
    }

    override fun getButtons(player: Player): MutableMap<Int, Button> {

        val toReturn = mutableMapOf<Int,Button>()

        toReturn[11] = TeamChestPurchaseButton(this.double,this.location,true)
        toReturn[15] = TeamChestPurchaseButton(this.double,this.location,false)

        return toReturn
    }

}