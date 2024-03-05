package cc.fyre.bunkers.claim

import net.hylist.HylistSpigot
import cc.fyre.bunkers.Bunkers

import cc.fyre.bunkers.claim.data.ClaimSelection
import cc.fyre.bunkers.claim.listener.ClaimListener
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.claim.listener.ClaimPositionListener
import cc.fyre.bunkers.claim.movement.ClaimMovementAdapter
import cc.fyre.engine.map.data.Map

import org.bukkit.ChatColor

import org.bukkit.entity.Player
import java.util.*


/**
 * @project hcf
 *
 * @date 04/04/2020
 * @author xanderume@gmail.com
 */
class ClaimHandler(private val instance: Bunkers) {

    val selections = HashMap<UUID, ClaimSelection>()

    init {
        HylistSpigot.INSTANCE.addMovementHandler(ClaimMovementAdapter(this.instance))

        this.instance.server.pluginManager.registerEvents(ClaimListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(ClaimMovementAdapter(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(ClaimPositionListener(this.instance),this.instance)
    }

    fun findSelection(uuid: UUID):Optional<ClaimSelection> {
        return Optional.ofNullable(this.selections[uuid])
    }

    fun startSelection(player: Player,team: Team,map: Map,slot: Int) {

        if (this.selections.remove(player.uniqueId) != null) {
            player.inventory.removeItem(ClaimSelection.ITEM)
        }

        player.inventory.setItem(slot,ClaimSelection.ITEM)
        player.sendMessage("${ChatColor.GREEN}You have been given a claiming wand.")

        this.selections[player.uniqueId] = ClaimSelection(map,team)
    }

    fun dispose() {
        this.selections.keys.map{this.instance.server.getPlayer(it)}.filter{it != null}.forEach{it.inventory.removeItem(ClaimSelection.ITEM)}
    }

}