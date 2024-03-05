package cc.fyre.bunkers.villager.data.kb

import net.hylist.knockback.KnockbackProfile

/**
 * @project bunkers
 *
 * @date 17/08/2020
 * @author xanderume@gmail.com
 */
class ZeroKBProfile : KnockbackProfile {

    private val name = "Zero"
    private var friction = 2.0
    private var vertical = 0.0
    private var horizontal = 0.0
    private var verticalLimit = 0.0
    private var extraVertical = 0.0
    private var extraHorizontal = 0.0

    override fun getName():String {
        return name
    }

    override fun getFriction(): Double {
        return friction
    }

    override fun setFriction(friction: Double) {
        this.friction = friction
    }

    override fun getHorizontal(): Double {
        return horizontal
    }

    override fun setHorizontal(horizontal: Double) {
        this.horizontal = horizontal
    }

    override fun getVertical(): Double {
        return vertical
    }

    override fun setVertical(vertical: Double) {
        this.vertical = vertical
    }

    override fun getVerticalLimit(): Double {
        return verticalLimit
    }

    override fun setVerticalLimit(verticalLimit: Double) {
        this.verticalLimit = verticalLimit
    }

    override fun getExtraHorizontal(): Double {
        return extraHorizontal
    }

    override fun setExtraHorizontal(extraHorizontal: Double) {
        this.extraHorizontal = extraHorizontal
    }

    override fun getExtraVertical(): Double {
        return extraVertical
    }

    override fun setExtraVertical(extraVertical: Double) {
        this.extraVertical = extraVertical
    }
}