package cc.fyre.bunkers.event.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import org.bukkit.command.CommandSender

object KOTHStartCommand {

    @JvmStatic
    @Command(names = ["koth start","event start"],permission = "op")
    fun execute(sender: CommandSender) {
        Bunkers.instance.eventHandler.setActive(!Bunkers.instance.eventHandler.isActive())
    }

}