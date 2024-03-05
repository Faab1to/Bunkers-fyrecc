package cc.fyre.bunkers.game

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.event.EventHandler
import cc.fyre.bunkers.game.deathmessage.BunkersDeathMessageConfiguration
import cc.fyre.bunkers.game.listener.*
import cc.fyre.bunkers.game.scoreboard.*

import cc.fyre.bunkers.team.TeamHandler
import cc.fyre.bunkers.team.data.Team

import cc.fyre.engine.GameEngine
import cc.fyre.engine.GameEngineAPI
import cc.fyre.engine.game.adapter.GameAdapter
import cc.fyre.engine.game.adapter.scoreboard.ScoreboardAdapter
import cc.fyre.engine.game.data.Game
import cc.fyre.engine.game.data.type.BunkersGame
import cc.fyre.engine.profile.data.type.BunkersProfile
import cc.fyre.engine.server.data.GameServer
import net.frozenorb.qlib.util.ItemBuilder
import com.google.common.collect.ImmutableSet
import com.google.gson.JsonObject
import net.frozenorb.qlib.deathmessage.FrozenDeathMessageHandler
import net.frozenorb.qlib.nametag.NametagInfo
import net.frozenorb.qlib.nametag.NametagProvider
import net.frozenorb.qlib.util.TimeUtils
import net.frozenorb.qlib.util.UUIDUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @project bunkers
 *
 * @date 13/08/2020
 * @author xanderume@gmail.com
 */
class BunkersGameAdapter(private val instance: Bunkers) : GameAdapter {

    init {

        FrozenDeathMessageHandler.setConfiguration(BunkersDeathMessageConfiguration)

        this.instance.server.pluginManager.registerEvents(GameListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(ChunkListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(VotingListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(LunarClientListener(this.instance),this.instance)
    }

    override fun onTick(seconds: Int) {

        if (seconds >= EventHandler.KOTH_START_TIME && !this.instance.eventHandler.isActive()) {
            this.instance.eventHandler.setActive(true)
        }

        if (EventHandler.CAPTURE_TIME_DECREASE.containsKey(seconds)) {
            this.instance.eventHandler.setMaxCaptureTime(EventHandler.CAPTURE_TIME_DECREASE[seconds]!!)
        }

        if (TeamHandler.RAIDABLE_BROADCASTS.contains(seconds) || (TeamHandler.RAIDABLE_TIME - seconds <= 5 && seconds <= TeamHandler.RAIDABLE_TIME)) {
            this.instance.server.broadcastMessage("${ChatColor.RED}${ChatColor.BOLD}All teams will go raidable in ${TimeUtils.formatIntoDetailedString((TeamHandler.RAIDABLE_TIME - seconds))}.")
        }

        if (seconds == TeamHandler.RAIDABLE_TIME) {
            this.instance.server.broadcastMessage("${ChatColor.RED}${ChatColor.BOLD}All teams are now raidable.")
            this.instance.teamHandler.findPlayerTeams().filter{!it.isRaidable()}.forEach{it.dtr = 0.0}
            return
        }

        val payload = JsonObject()

        payload.addProperty("GAME_TIME",seconds * 1000L)

        if (this.instance.eventHandler.isActive()) {

            val zoneData = JsonObject()

            zoneData.addProperty("CAP_ZONE_NAME",this.instance.teamHandler.cache[Team.Type.KOTH]?.getDisplayName() ?: "")
            zoneData.addProperty("CAP_ZONE_TIME",this.instance.eventHandler.service.remaining.get())

            payload.add("CAP_ZONE_DATA",zoneData)
        }

        this.instance.teamHandler.findPlayerTeams().forEach{payload.addProperty("${it.type.name}_DTR",it.getDTRDisplay())}

        GameEngine.instance.gameHandler.addMetadata(payload)
    }

    override fun onGameFinish() {

        val team = this.instance.eventHandler.controlledByTeam

        val messages = ArrayList<String>()

        messages.add(Team.LINE)
        messages.add(" ")
        messages.add(StringUtils.center("${ChatColor.GOLD}Winner: ${team?.getDisplayName() ?: "${ChatColor.WHITE}Unknown"}",Team.LINE.length))

        if (team != null) {
            messages.add(StringUtils.center(StringUtils.join(team.members.map{UUIDUtils.name(it)}.toTypedArray(),"${ChatColor.GRAY}, "),Team.LINE.length))
        }

        messages.add(" ")

        val kills = this.instance.statisticHandler.kills.entries.sortedByDescending{it.value}.take(3)

        messages.add(StringUtils.center("${ChatColor.GOLD}Top Kills:",Team.LINE.length))

        val colors = arrayOf(ChatColor.DARK_GREEN,ChatColor.GREEN,ChatColor.GOLD)

        kills.withIndex().forEach{messages.add(StringUtils.center("${colors[it.index]}#${it.index + 1} ${UUIDUtils.name(it.value.key)}${ChatColor.GRAY}: ${it.value.value}",Team.LINE.length))}

        messages.add(" ")
        messages.add(Team.LINE)

        messages.forEach{this.instance.server.broadcastMessage(it)}
    }

    override fun onPlayersSent(players: ArrayList<UUID>) {
        GameEngine.instance.gameHandler.parties.sortedByDescending{it.size}.forEach{this.instance.teamHandler.addToTeam(it)}
        //this.loadParties(ArrayList(GameEngine.instance.gameHandler.parties.sortedBy{it.size}))
    }

    override fun onGameAddPlayer(player: Player) {

        val team = this.instance.teamHandler.findById(player.uniqueId)

        if (team != null) {
            return
        }

        if (this.instance.teamHandler.addToTeam(player.uniqueId) != null) {
            return
        }

        GameEngine.instance.spectateHandler.addSpectator(player)
    }

    override fun onGameAddSpectator(player: Player) {

        val team = this.instance.teamHandler.cache[Team.Type.KOTH] ?: return

        player.teleport(team.hq)
    }

    override fun onGameDisqualifiePlayer(uuid: UUID) {

        val name = "${this.instance.teamHandler.findById(uuid)?.type?.color ?: ChatColor.WHITE}${UUIDUtils.name(uuid)}"

        this.instance.server.broadcastMessage("$name${ChatColor.GOLD}[${this.instance.statisticHandler.getKills(uuid)}]${ChatColor.RED} has been disqualified.")
    }

    override fun onGameRemovePlayer(player: Player) {}

    override fun getNameTag(player: Player,target: Player):NametagInfo? {

        val targetTeam = this.instance.teamHandler.findById(target.uniqueId)

        if (targetTeam != null && targetTeam.isFocused(player)) {
            return NametagProvider.createNametag(ChatColor.LIGHT_PURPLE.toString(),"")
        }

        val team = this.instance.teamHandler.findById(player.uniqueId)

        if (team != null) {
            val nameTagEntry = NametagProvider.createNametag(team.type.color.toString(),"")

            if (targetTeam == null || !targetTeam.getName().equals(team.getName(), true)) {
                //TODO nameTagEntry.allowFriendlyVisibility = false
            }

            if (targetTeam != null && targetTeam.getName().equals(team.getName(), true)) {
                //TODO nameTagEntry.allowFriendlyVisibility = true
            }

            return NametagProvider.createNametag(team.type.color.toString(),"")
        }

        return null
    }

    override fun getScoreboardAdapters(): HashMap<GameServer.State, ScoreboardAdapter> {

        val toReturn = hashMapOf<GameServer.State,ScoreboardAdapter>()

        toReturn[GameServer.State.WAITING] = WaitingScoreboardAdapter()
        toReturn[GameServer.State.VOTING] = VotingScoreboardAdapter()
        toReturn[GameServer.State.COUNTDOWN] = CountdownScoreboardAdapter()
        toReturn[GameServer.State.IN_PROGRESS] = InProgressScoreboardAdapter()

        return toReturn
    }
    override fun getEndGameData():Game {

        var team: Team? = null

        if (this.instance.eventHandler.controlledBy != null) {
            team = this.instance.teamHandler.findById(this.instance.eventHandler.controlledBy!!)
        }

        val teams = ArrayList<BunkersGame.Team>()

        this.instance.teamHandler.findPlayerTeams().forEach{teams.add(BunkersGame.Team(it.getName(),it.getColor().toString(),it.dtr,it.members.toCollection(ArrayList())))}

        return BunkersGame(
                GameEngine.instance.gameHandler.getGameTime(),
                GameEngine.instance.gameHandler.map.id,
                teams,
                this.instance.statisticHandler.kills,
                this.instance.statisticHandler.deaths,
                this.instance.statisticHandler.playTime,
                this.instance.statisticHandler.oresMined.entries.associate{it.key to it.value.entries.sumBy{entry -> entry.value}}.toMap(HashMap()),
                this.instance.statisticHandler.playTimeClass.entries.associate{it.key to it.value.entries.maxByOrNull{entry -> entry.value}?.key}.toMap(HashMap()),
                team?.getName(),
                this.instance.eventHandler.controlledBy
        )
    }

    override fun onGameFinishUpdateProfiles() {

        val controller = this.instance.eventHandler.controlledBy
        val winningTeam = if (controller == null) null else this.instance.teamHandler.findById(controller)

        this.instance.teamHandler.findPlayerTeams().forEach{team ->

            for (member in team.members) {

                var profile = GameEngine.instance.api.profileHandler.findById(member,GameEngineAPI.GameMode.BUNKERS) as BunkersProfile?

                if (profile == null) {
                    profile = BunkersProfile(member)
                }

                val player = this.instance.server.getPlayer(profile.id)

                if (player != null) {

                    val pvpClass = this.instance.pvpClassHandler.findById(player.uniqueId)

                    if (pvpClass != null) {
                        this.instance.statisticHandler.recalculatePlayTime(player.uniqueId,pvpClass.type.backendType)
                    }

                    this.instance.statisticHandler.recalculatePlayTime(player.uniqueId)
                }

                if (winningTeam != null && winningTeam.isMember(profile.id) && player != null) {

                    if (GameEngine.instance.gameHandler.ranked) {
                        profile.rankedWins++
                    } else {
                        profile.normalWins++
                    }

                    profile.totalWins++
                } else {
                    if (GameEngine.instance.gameHandler.ranked) {
                        profile.rankedLosses++
                    } else {
                        profile.normalLosses++
                    }

                    profile.totalLosses++
                }

                profile.gamesPlayed++
                profile.kills += this.instance.statisticHandler.getKills(member)
                profile.deaths += this.instance.statisticHandler.getDeaths(member)
                profile.playTime += this.instance.statisticHandler.getPlayTime(member)
                profile.oresMined += this.instance.statisticHandler.getOresMined(member)

                BunkersProfile.Ore.values().forEach{
                    profile.oresTypeMined[it] = (profile.oresTypeMined[it] ?: 0) + this.instance.statisticHandler.getOresMined(member,it)
                }

                BunkersProfile.PvPClass.values().forEach{
                    profile.classPlayTime[it] = (profile.classPlayTime[it] ?: 0L) + this.instance.statisticHandler.getPlayTime(member,it)
                }

                GameEngine.instance.api.profileHandler.update(profile)
            }

        }
    }

    companion object {

        val ANTIDOTE = ItemBuilder.of(Material.POTION).name("${ChatColor.RED}${ChatColor.BOLD}Antidote").data(8228).build()
        val LESSER_INVISIBILITY = ItemBuilder.of(Material.POTION).name("${ChatColor.WHITE}Lesser Invisibility").data(8270).build()

        val STARTER_ITEMS = ImmutableSet.of(
            ItemBuilder.of(Material.STONE_AXE).addToLore("${ChatColor.GOLD}Soulbound").build(),
            ItemBuilder.of(Material.STONE_PICKAXE).addToLore("${ChatColor.GOLD}Soulbound").build()
        )

        init {

            val itemMeta = LESSER_INVISIBILITY.itemMeta as PotionMeta

            itemMeta.addCustomEffect(PotionEffect(PotionEffectType.INVISIBILITY,(6*60) * 20,2),true)

            LESSER_INVISIBILITY.itemMeta = itemMeta
        }

    }

    // Old method use this one if current isn't good

    /*
    fun loadParties(parties: ArrayList<ArrayList<UUID>>) {

        Bunkers.instance.server.logger.info("--- Party Sets ---")
        parties.forEach{Bunkers.instance.server.logger.info(StringUtils.join(arrayListOf(it).flatMap{ members -> members.map{ member -> UUIDUtils.name(member)}},", "))}
        Bunkers.instance.server.logger.info("------------------")
        Bunkers.instance.server.logger.info(" ")
        Bunkers.instance.server.logger.info("--- Full Sets ---")
        parties.forEach{Bunkers.instance.server.logger.info(StringUtils.join(arrayListOf(parties.filter{it.size >= TeamHandler.PLAYERS_PER_TEAM}).flatMap{ members -> members.flatten().map{ member -> UUIDUtils.name(member)}},", "))}
        Bunkers.instance.server.logger.info("------------------")

        val oddParties = ArrayList<ArrayList<UUID>>()

        parties.removeIf{

            if (it.size < TeamHandler.PLAYERS_PER_TEAM) {
                return@removeIf false
            }

            val team = Bunkers.instance.teamHandler.cache.values.first{team -> !team.type.isSystem() && team.members.size == 0}
            val oddMembers = ArrayList<UUID>()

            for (uuid in it) {

                if (team.members.size >= TeamHandler.PLAYERS_PER_TEAM) {

                    Bunkers.instance.server.logger.info("--- Odd Man Out ---")
                    Bunkers.instance.server.logger.info("${UUIDUtils.name(uuid)} -> ${team.type}")
                    Bunkers.instance.server.logger.info("-----------------------")

                    oddMembers.add(uuid)
                    continue
                }

                Bunkers.instance.server.logger.info("--- Add Member ---")
                Bunkers.instance.server.logger.info("${UUIDUtils.name(uuid)} -> ${team.type}")
                Bunkers.instance.server.logger.info("-----------------------")

                team.members.add(uuid)
            }

            oddParties.add(oddMembers)
            return@removeIf true
        }

        for (party in parties) {

            val foundPlayers = ArrayList<UUID>()

            for (possibleParty in parties.filter{it != party}) {

                if (foundPlayers.size + possibleParty.size > TeamHandler.PLAYERS_PER_TEAM) {
                    continue
                }

                foundPlayers.addAll(possibleParty)
            }

            if (foundPlayers.size == TeamHandler.PLAYERS_PER_TEAM) {
                parties.forEach{nigger -> nigger.removeIf{foundPlayers.contains(it)}}
                Bunkers.instance.teamHandler.cache.values.first{team -> !team.type.isSystem() && team.members.size == 0}.members.addAll(foundPlayers)
            }

        }

        if (oddParties.isNotEmpty()) {
            oddParties.sortedBy{it.size}.forEach{members -> members.forEach{Bunkers.instance.teamHandler.cache.values.first{team -> !team.type.isSystem() && team.members.size < TeamHandler.PLAYERS_PER_TEAM}.members.add(it)}}
        }

        if (parties.isNotEmpty()) {
            parties.sortedBy{it.size}.forEach{members -> members.forEach{Bunkers.instance.teamHandler.cache.values.first{team -> !team.type.isSystem() && team.members.size < TeamHandler.PLAYERS_PER_TEAM}.members.add(it)}}
        }

        Bunkers.instance.teamHandler.cache.values.forEach{Bunkers.instance.server.logger.info("${it.type} -> ${StringUtil.join(it.members.map{ member -> UUIDUtils.name(member)}.toTypedArray(),", ")}")}
    }*/

}