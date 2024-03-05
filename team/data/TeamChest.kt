package cc.fyre.bunkers.team.data

import java.util.*

data class TeamChest(val owner: UUID,val double: Boolean,val members: MutableSet<UUID> = mutableSetOf()) {

    fun isMember(uuid: UUID):Boolean {
        return uuid == this.owner || this.members.contains(uuid)
    }

}