package cc.fyre.bunkers.shop.service

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.shop.ShopHandler
import cc.fyre.bunkers.shop.data.ShopType
import cc.fyre.bunkers.team.data.Team
import net.frozenorb.qlib.hologram.FrozenHologramHandler
import net.frozenorb.qlib.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.atomic.AtomicInteger

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
class ShopService(private val location: Location,private val shop: ShopType,private val team: Team.Type):BukkitRunnable() {

    private val hologram = FrozenHologramHandler.createHologram()
        .at(this.location.clone().add(0.0,1.6,0.0))
        .build()

    private val remaining = AtomicInteger(ShopHandler.RESPAWN_SECONDS)

    init {
        this.hologram.addLines("${this.team.color}${this.shop.getDisplayName()}","${ChatColor.GRAY}Respawns in ${ChatColor.YELLOW}${TimeUtils.formatIntoHHMMSS(this.remaining.get())}")
        FrozenHologramHandler.getCache().add(this.hologram)
    }

    override fun run() {

        if (this.remaining.get() <= 0) {
            this.hologram.destroy()

            FrozenHologramHandler.getCache().remove(this.hologram)
            Bunkers.instance.shopHandler.spawnVillager(this.location,this.shop,this.team)

            this.cancel()
            return
        }

        this.hologram.setLine(1,"${ChatColor.GRAY}Respawns in ${ChatColor.YELLOW}${TimeUtils.formatIntoHHMMSS(this.remaining.decrementAndGet())}")
    }

}