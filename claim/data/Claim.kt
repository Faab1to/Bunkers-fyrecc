package cc.fyre.bunkers.claim.data

import cc.fyre.bunkers.Bunkers
import cc.fyre.bunkers.team.data.Team
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.max
import kotlin.math.min

/**
 * @project hcf
 *
 * @date 04/04/2020
 * @author xanderume@gmail.com
 */
class Claim(world: World,x1: Int,x2: Int,z1: Int,z2: Int) {

    constructor(first: Location,second: Location):this(first.world,first.blockX,second.blockX,first.blockZ,second.blockZ)

    private val x1 = min(x1,x2)
    private val x2 = max(x1,x2)
    private val z1 = min(z1,z2)
    private val z2 = max(z1,z2)

    private val world = world.name

    fun findWorld():World? = Bunkers.instance.server.getWorld(this.world)

    fun contains(x: Int,z: Int,world: World):Boolean {
        return x >= this.x1 && x <= this.x2 && z >= this.z1 && z <= this.z2 && this.world == world.name
    }

    fun contains(location: Location):Boolean {

        val world = this.findWorld() ?: return false

        return this.contains(location.blockX,location.blockZ,world)
    }

    fun getLower():Location {
        return Location(this.findWorld(),this.x1.toDouble(),0.0,this.z1.toDouble())
    }

    fun getUpper():Location {
        return Location(this.findWorld(),this.x2.toDouble(),256.0,this.z2.toDouble())
    }

    fun borderIterator():Iterator<Pair<Int,Int>> {
        return BorderIterator(this,this.x1,this.z1,this.x2,this.z2)
    }

    // HCTeams can suck my dick nigga
    class BorderIterator(val claim: Claim,x1: Int, z1: Int, x2: Int, z2: Int) : MutableIterator<Pair<Int,Int>> {

        private var x: Int = min(x1,x2)
        private var z: Int = min(z1,z2)

        private var next = true
        private var direction = BorderDirection.POS_Z

        private var minX: Int = this.claim.getLower().blockX
        private var minZ: Int = this.claim.getLower().blockZ
        private var maxX: Int = this.claim.getUpper().blockX
        private var maxZ: Int = this.claim.getUpper().blockZ

        override fun hasNext(): Boolean {
            return this.next
        }

        override fun next():Pair<Int,Int> {
            if (this.direction == BorderDirection.POS_Z) {
                if (++this.z == this.maxZ) {
                    this.direction = BorderDirection.POS_X
                }
            } else if (this.direction == BorderDirection.POS_X) {
                if (++this.x == this.maxX) {
                    this.direction = BorderDirection.NEG_Z
                }
            } else if (this.direction == BorderDirection.NEG_Z) {
                if (--this.z == this.minZ) {
                    this.direction = BorderDirection.NEG_X
                }
            } else if (this.direction == BorderDirection.NEG_X) {
                if (--this.x == this.minX) {
                    this.next = false
                }
            }
            return Pair(this.x,this.z)
        }

        override fun remove() {}

        enum class BorderDirection {

            POS_X,
            POS_Z,
            NEG_X,
            NEG_Z

        }

    }
}