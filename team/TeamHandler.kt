package cc.fyre.bunkers.team
/**
 * @project bunkers
 *
 * @date 30/07/2020
 * @author xanderume@gmail.com
 */

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.command.*
import cc.fyre.bunkers.team.command.parameter.TeamParameterProvider
import cc.fyre.bunkers.team.data.Team
import cc.fyre.bunkers.team.listener.*
import cc.fyre.engine.GameEngine
import cc.fyre.engine.GameEngineAPI
import cc.fyre.engine.map.data.Map
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import net.frozenorb.qlib.command.FrozenCommandHandler
import net.frozenorb.qlib.qLib
import net.frozenorb.qlib.util.UUIDUtils
import org.apache.commons.lang.StringUtils
import org.bson.Document
import org.bukkit.ChatColor
import org.bukkit.Location

import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

//TODO: team loading & saving is kinda ass
class TeamHandler(private val instance: Bunkers) {

    val cache = HashMap<Team.Type,Team>()
    val collection = GameEngine.instance.api.databaseHandler.mongoDB.getCollection("teams")

    init {
        Team.Type.values().forEach{this.cache[it] = Team(it)}

        this.instance.server.pluginManager.registerEvents(TeamListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(TeamChatListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(TeamChestListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(TeamRallyListener(this.instance),this.instance)
        this.instance.server.pluginManager.registerEvents(TeamProtectionListener(this.instance),this.instance)

        FrozenCommandHandler.registerClass(TeamHQCommand::class.java)
        FrozenCommandHandler.registerClass(TeamInfoCommand::class.java)
        FrozenCommandHandler.registerClass(TeamChatCommand::class.java)
        FrozenCommandHandler.registerClass(TeamSetHQCommand::class.java)
        FrozenCommandHandler.registerClass(TeamSetShopCommand::class.java)
        FrozenCommandHandler.registerClass(TeamClaimCommand::class.java)
        FrozenCommandHandler.registerClass(TeamFocusCommand::class.java)
        FrozenCommandHandler.registerClass(TeamRallyCommand::class.java)
        FrozenCommandHandler.registerClass(TeamLocationCommand::class.java)
        FrozenCommandHandler.registerClass(TeamVoteKickCommand::class.java)
        FrozenCommandHandler.registerClass(TeamSetDTRCommand::class.java)
        FrozenCommandHandler.registerClass(TeamSetHologramCommand::class.java)
        FrozenCommandHandler.registerParameterType(Team::class.java,TeamParameterProvider())
    }

    fun findById(uuid: UUID):Team? {
        return this.cache.values.firstOrNull{it.members.contains(uuid)}
    }

    fun findByLocation(location: Location):Team {
        return this.cache.values.firstOrNull{it.claim != null && it.claim!!.contains(location)} ?: (this.cache[Team.Type.WAR_ZONE] ?: Team(Team.Type.WAR_ZONE))
    }

    fun findSystemTeams():MutableSet<Team> {
        return this.cache.values.filter{it.type.isSystem()}.toMutableSet()
    }

    fun findPlayerTeams():MutableSet<Team> {
        return this.cache.values.filter{!it.type.isSystem()}.toMutableSet()
    }

    fun loadTeamDataSync(map: Map) {

        val document = this.collection.find(Filters.eq("_id",map.id)).firstOrNull() ?: return

        Team.Type.values().filter{document.containsKey(it.name)}.forEach{
            val team = qLib.GSON.fromJson(Document.parse(document.getString(it.name)).toJson(GameEngineAPI.JSON_WRITER_SETTINGS),Team::class.java)

            val cachedTeam = this.cache[it]!!

            cachedTeam.hq = team.hq
            cachedTeam.claim = team.claim
            cachedTeam.shops.putAll(team.shops)
            cachedTeam.holograms.addAll(team.holograms)
        }

    }

    fun saveTeamData(team: Team,map: Map):UpdateResult {

        val document = this.collection.find(Filters.eq("_id",map.id)).firstOrNull() ?: Document("_id",map.id)

        document.append(team.type.name,qLib.GSON.toJson(team))

        return this.collection.updateOne(Document("_id",map.id),Document("\$set",document), UpdateOptions().upsert(true))
    }

    fun addToTeam(uuid: UUID):Team? {

        val team = Bunkers.instance.teamHandler.findPlayerTeams().filter{!it.isFull()}.maxByOrNull{it.getAvailableSlots()}

        if (team == null) {

            val player = this.instance.server.getPlayer(uuid)

            GameEngine.instance.gameHandler.removePlayer(player)
            player.kickPlayer("${ChatColor.RED}We we're unable find you a team!")
            return null
        }

        this.instance.logger.info("Added ${UUIDUtils.name(uuid)} to ${team.getDisplayName()} team.")

        team.members.add(uuid)
        return team
    }

    fun addToTeam(players: ArrayList<UUID>) {

        val team = this.findPlayerTeams().find{it.getAvailableSlots() >= players.size} ?: this.findPlayerTeams().maxByOrNull{it.getAvailableSlots()}

        if (team == null) {
            players.forEach{this.addToTeam(it)}
            return
        }

        players.filter{Bunkers.instance.teamHandler.findById(it) == null}.forEach{

            if (team.isFull()) {
                this.addToTeam(it)
                this.instance.logger.info("Team ${team.getDisplayName()} was full, moved ${UUIDUtils.name(it)} to another team.")
            } else {
                team.members.add(it)
            }

        }

        this.instance.logger.info("Added ${StringUtils.join(team.members.map{UUIDUtils.name(it)}.toTypedArray(),", ")} to ${team.getDisplayName()} team.")
    }

    companion object {

        const val CHEST_PRICE = 500
        const val PLAYERS_PER_TEAM = 5

        val RALLY_TIME = TimeUnit.MINUTES.toSeconds(5L).toInt()
        val RAIDABLE_TIME = TimeUnit.HOURS.toSeconds(1L).toInt()
        val RAIDABLE_BROADCASTS = mutableListOf(TimeUnit.MINUTES.toSeconds(30L).toInt(),TimeUnit.MINUTES.toSeconds(40L).toInt(),TimeUnit.MINUTES.toSeconds(50L).toInt(),TimeUnit.MINUTES.toSeconds(55L).toInt())

    }

}