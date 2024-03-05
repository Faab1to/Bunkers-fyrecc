package cc.fyre.bunkers.team.data

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.claim.data.Claim
import cc.fyre.bunkers.shop.data.ShopType
import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.timer.data.TimerType

import cc.fyre.engine.GameEngine
import cc.fyre.engine.server.data.GameServer
import com.google.gson.annotations.Expose
import mkremins.fanciful.FancyMessage
import net.frozenorb.qlib.util.UUIDUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * @project bunkers
 *
 * @date 30/07/2020
 * @author xanderume@gmail.com
 */
class Team(val type: Type) {

    var hq: Location? = null
    var claim: Claim? = null
    val shops =  HashMap<ShopType,Location>()
    val holograms = HashSet<Location>()

    var focus: UUID? = null

    @Transient var dtr = TeamHandler.PLAYERS_PER_TEAM + 1.0
    @Transient val chat = mutableMapOf<UUID,Boolean>()
    @Transient var votes = mutableMapOf<UUID,ArrayList<UUID>>()
    @Transient val members = hashSetOf<UUID>()

    @Transient var chests = mutableMapOf<Location,TeamChest>()

    @Transient var rally: Location? = null
    @Transient var rallyTask: BukkitTask? = null

    fun isFull():Boolean {
        return this.members.size >= TeamHandler.PLAYERS_PER_TEAM
    }

    fun getAvailableSlots():Int {
        return TeamHandler.PLAYERS_PER_TEAM - this.members.size
    }

    fun isMember(uuid: UUID):Boolean {
        return this.members.contains(uuid)
    }

    fun isMember(player: Player):Boolean {
        return this.members.contains(player.uniqueId)
    }

    fun isRaidable():Boolean {
        return this.dtr <= 0.0
    }

    fun getDTR():String {
        return DTR_FORMAT.format(this.dtr)
    }

    fun isFocused(player: Player):Boolean {
        return this.focus != null && this.focus == player.uniqueId
    }


    fun getDTRDisplay():String {

        var color = ChatColor.GREEN

        when {
            this.dtr <= 0.0 -> color =  ChatColor.GOLD
            this.dtr > 1.0 && this.dtr <= 2.0 -> color = ChatColor.YELLOW
            this.dtr > 0.0 && this.dtr <= 1.0 -> color = ChatColor.RED
            this.dtr <= (TeamHandler.PLAYERS_PER_TEAM + 1.0) / 2 -> color = ChatColor.DARK_RED
        }

        return "$color${this.getDTR()}"
    }

    fun getColor():ChatColor {
        return this.type.color
    }

    fun getDisplayName():String {
        return this.type.getDisplayName()
    }

    fun sendMessage(message: FancyMessage) {
        this.members.mapNotNull{Bunkers.instance.server.getPlayer(it)}.forEach{message.send(it)}
    }

    fun sendMessage(message: String) {
        this.sendMessage(*arrayOf(message))
    }

    fun sendMessage(vararg message: String) {
        this.members.mapNotNull{Bunkers.instance.server.getPlayer(it)}.forEach{it.sendMessage(message)}
    }

    fun sendMessage(message: String,vararg ignore: UUID) {
        this.members.filter{!ignore.contains(it)}.mapNotNull{Bunkers.instance.server.getPlayer(it)}.forEach{it.sendMessage(message)}
    }

    fun sendInfo(sender: CommandSender) {
        sender.sendMessage(LINE)

        if (this.type.isSystem()) {
            sender.sendMessage(this.getDisplayName())
            sender.sendMessage("${ChatColor.YELLOW}Location: ${ChatColor.WHITE}${if (this.hq == null) "None" else "${this.hq!!.blockX}, ${this.hq!!.blockZ}"}")
        } else {
            sender.sendMessage("${this.getDisplayName()} ${ChatColor.GRAY}[${this.findOnlineMembers().size}/${this.members.size}]${ChatColor.DARK_AQUA} - ${ChatColor.YELLOW}HQ: ${ChatColor.WHITE}${if (this.hq == null) "None" else "${this.hq!!.blockX}, ${this.hq!!.blockZ}"}")

            if (this.members.isNotEmpty()) {
                sender.sendMessage("${ChatColor.YELLOW}Members: ${StringUtils.join(this.members.map{"${this.getDisplayName(it)}${ChatColor.YELLOW}[${ChatColor.GREEN}${Bunkers.instance.statisticHandler.getKills(it)}${ChatColor.YELLOW}]"}.toTypedArray(),"${ChatColor.GRAY}, ")}")
            }

            sender.sendMessage("${ChatColor.YELLOW}Deaths Until Raidable: ${this.getDTRDisplay()}")
        }

        sender.sendMessage(LINE)
    }

    fun getDisplayName(uuid: UUID): String {

        val player = Bunkers.instance.server.getPlayer(uuid)

        if (player != null) {

            if (GameEngine.instance.spectateHandler.isSpectating(player)) {
                return "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${player.name}"
            }

            val respawn = Bunkers.instance.timerHandler.findRemaining(uuid,TimerType.RESPAWN)

            if (respawn > 0L) {
                return "${ChatColor.RED}${player.name}"
            }

            return "${ChatColor.GREEN}${player.name}"
        }

        val name = UUIDUtils.name(uuid)

        if (GameEngine.instance.disqualifieHandler.isDisqualified(uuid)) {
            return "${ChatColor.DARK_GRAY}${ChatColor.STRIKETHROUGH}${name}"
        }

        return "${ChatColor.GRAY}$name"
    }

    fun findChestByBlock(block: Block):TeamChest? {

        val state = block.state

        if (state !is Chest) {
            return null
        }

        val holder = state.inventory.holder

        return if (holder is DoubleChest) {
            this.chests[holder.location]
        } else {
            this.chests[block.location]
        }
    }

    fun findAliveMembers():MutableSet<UUID> {
        return this.members.filter{!(GameEngine.instance.spectateHandler.isSpectating(it) || GameEngine.instance.disqualifieHandler.isDisqualified(it))}.toMutableSet()
    }

    fun findOnlineMembers():MutableList<Player> {
        return this.members.mapNotNull{Bunkers.instance.server.getPlayer(it)}.toMutableList()
    }

    fun getName(): String {
        return WordUtils.capitalizeFully(this.type.name.replace("_"," ")).replace(" ","")
    }

    enum class Type(val color: ChatColor) {

        RED(ChatColor.RED),
        BLUE(ChatColor.BLUE),
        GREEN(ChatColor.GREEN),
        YELLOW(ChatColor.YELLOW),
        KOTH(ChatColor.AQUA),
        WAR_ZONE(ChatColor.DARK_RED);

        fun isSystem():Boolean {
            return this == WAR_ZONE || this == KOTH
        }

        fun getDisplayName():String {

            if (this == KOTH && (GameEngine.instance.gameHandler.getState().isPastOrCurrently(GameServer.State.COUNTDOWN))) {
                return "${this.color}${GameEngine.instance.gameHandler.map.id} ${ChatColor.GOLD}KOTH"
            }

            return "${this.color}${WordUtils.capitalizeFully(this.name.replace("_"," ")).replace(" ","")}"
        }

        companion object {

            fun findByName(name: String):Type? {
                return values().firstOrNull{it.name.equals(name,true)}
            }

        }

    }

    companion object {

        val DTR_FORMAT = DecimalFormat("0.0")

        val LINE = "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-",53)}"
    }
}