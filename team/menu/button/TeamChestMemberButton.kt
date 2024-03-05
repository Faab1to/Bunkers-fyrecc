package cc.fyre.bunkers.team.menu.button

import cc.fyre.bunkers.team.data.TeamChest
import cc.fyre.engine.GameEngine
import net.frozenorb.qlib.menu.Button
import net.frozenorb.qlib.util.UUIDUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.*

class TeamChestMemberButton(private val chest: TeamChest,private val member: UUID) : Button() {

    private val disqualified = GameEngine.instance.disqualifieHandler.isDisqualified(this.member)

    override fun getName(p0: Player?): String {

        val color = if (this.disqualified) ChatColor.GRAY else if (this.chest.isMember(this.member)) ChatColor.GREEN else ChatColor.RED

        return "$color${ChatColor.BOLD}${if (this.disqualified) ChatColor.STRIKETHROUGH else ""}${UUIDUtils.name(this.member)}"
    }


    override fun getDescription(p0: Player?): MutableList<String> {
        return mutableListOf()
    }

    override fun getMaterial(p0: Player?): Material {
        return Material.SKULL_ITEM
    }

    override fun clicked(player: Player?, slot: Int, clickType: ClickType?) {

        if (this.disqualified) {
            return
        }

        if (this.chest.isMember(this.member)) {
            this.chest.members.remove(this.member)
        } else {
            this.chest.members.add(this.member)
        }


    }
}