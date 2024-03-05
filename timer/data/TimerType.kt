package cc.fyre.bunkers.timer.data

import org.bukkit.ChatColor
import org.bukkit.Material
import java.util.concurrent.TimeUnit

/**
 * @project hcf
 *
 * @date 06/07/2020
 * @author xanderume@gmail.com
 */
enum class TimerType(val duration: Long,val scoreboard: String,val displays: Boolean,val icon: Material?) {

    HOME(TimeUnit.SECONDS.toMillis(10L),"${ChatColor.BLUE}${ChatColor.BOLD}HQ",true,Material.WOOD_DOOR),
    RESPAWN(TimeUnit.SECONDS.toMillis(15L),"${ChatColor.GOLD}${ChatColor.BOLD}Respawn",true,Material.LEASH),
    ANTIDOTE(TimeUnit.SECONDS.toMillis(5L),"${ChatColor.RED}${ChatColor.BOLD}Antidote",true,Material.SPIDER_EYE),
    ENDER_PEARL(TimeUnit.SECONDS.toMillis(16L),"${ChatColor.YELLOW}${ChatColor.BOLD}Enderpearl",true,Material.ENDER_PEARL),
    ENERGY_COOLDOWN(TimeUnit.SECONDS.toMillis(10L),"${ChatColor.GREEN}${ChatColor.BOLD}Energy Effect",false,Material.GOLD_HELMET);

    companion object {

        fun findByName(name: String): TimerType? {
            return values().firstOrNull{it.name.equals(name,true) || it.name.replace("_", "").equals(name,true) || ChatColor.stripColor(it.scoreboard.replace(" ", "")).equals(name,true)}
        }

    }

}