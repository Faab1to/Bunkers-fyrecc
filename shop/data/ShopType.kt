package cc.fyre.bunkers.shop.data

import cc.fyre.bunkers.shop.menu.BuildMenu
import cc.fyre.bunkers.shop.menu.CombatMenu
import cc.fyre.bunkers.shop.menu.EnchantMenu
import cc.fyre.bunkers.shop.menu.SellMenu
import net.frozenorb.qlib.menu.Menu
import org.apache.commons.lang.StringUtils

/**
 * @project bunkers
 *
 * @date 18/08/2020
 * @author xanderume@gmail.com
 */
enum class ShopType(val menu: Menu) {

    SELL(SellMenu()),
    BUILD(BuildMenu()),
    COMBAT(CombatMenu()),
    ENCHANT(EnchantMenu());

    fun getDisplayName():String {
        return "${StringUtils.capitalize(this.name.toLowerCase())} Shop"
    }

    companion object {

        fun findByName(name: String): ShopType? {
            return values().firstOrNull{it.name.equals(name,true)}
        }

    }

}