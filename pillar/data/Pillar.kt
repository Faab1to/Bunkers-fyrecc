package cc.fyre.bunkers.pillar.data

import org.bukkit.Location
import org.bukkit.Material

/**
 * @project hcf
 *
 * @date 27/08/2020
 * @author xanderume@gmail.com
 */
class Pillar(val type: Type,val location: Location,val material: Material,val data: Short) {

    constructor(type: Type,location: Location,material: Material):this(type,location,material,0)

    enum class Type(val materials: Array<Material>) {

        CLAIM_BORDER(arrayOf(Material.STAINED_GLASS));

    }

}