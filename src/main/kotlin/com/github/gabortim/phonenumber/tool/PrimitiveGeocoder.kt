package com.github.gabortim.phonenumber.tool

import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.tools.Territories

/**
 * Provides a fast method for getting a primitive country ISO 3166-1 Alpha-2 code by caching recently queried codes.
 */
internal object PrimitiveGeocoder {
    // we are interested in ISO 3166-1 Alpha-2 codes only
    private val countryCodes: Set<String> = Territories.getKnownIso3166Codes().map { it.take(2) }.toSet()
    private val nearbyCountryCodes: MutableSet<String> = HashSet(6)

    /**
     * Adds the primitive's ISO 3166-1 Alpha-2 code to cached countries, by calculating the center of the feature.
     * Can be inaccurate because of the internal dataset precision!
     *
     * @param primitive Currently checked object
     * @return The ISO 3166-1 Alpha-2 code which the given OSM primitive center resides.
     */
    private fun addNearbyCountry(primitive: OsmPrimitive): String {
        for (country in countryCodes) {
            if (country != "EU" && Territories.isIso3166Code(country, primitive.bBox.center)) {
                // TODO: check if object spans across border
                nearbyCountryCodes.add(country)
                return country
            }
        }
        return ""
    }

    /**
     * Calculates the ISO 3166-1 Alpha-2 code of the given primitive.
     *
     * @param primitive Currently checked object
     * @return The ISO 3166-1 Alpha-2 code which the given OSM primitive center resides.
     */
    fun getIso3166Alpha2Code(primitive: OsmPrimitive): String {
        val currentCountry = nearbyCountryCodes.find {
            Territories.isIso3166Code(it, primitive.bBox.center)
        }

        return currentCountry ?: addNearbyCountry(primitive)
    }

    /**
     * Clears cached collected county codes. It is safe to call anytime.
     */
    fun clear() {
        nearbyCountryCodes.clear()
    }
}
