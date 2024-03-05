package cc.fyre.bunkers.event.command

import cc.fyre.bunkers.Bunkers
import net.frozenorb.qlib.command.Command
import net.frozenorb.qlib.command.Param
import org.bukkit.command.CommandSender

object KOTHSetTimeCommand {

    @JvmStatic
    @Command(names = ["koth settime","event settime"],permission = "op")
    fun execute(sender: CommandSender, @Param(name = "seconds")seconds: Int) {
        Bunkers.instance.eventHandler.service.remaining.set(seconds)
    }


}