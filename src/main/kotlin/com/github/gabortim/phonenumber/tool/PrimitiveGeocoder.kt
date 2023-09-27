package com.github.gabortim.phonenumber.tool

import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.tools.Territories

/**
 * Provides a fast method for getting a primitive country ISO code.
 */
internal object PrimitiveGeocoder {
    // we are interested in ISO 3166 Alpha-2 codes only
    private val countryCodes: Set<String> = Territories.getKnownIso3166Codes().map { it.take(2) }.toSet()
    private val nearbyCountryCodes: MutableSet<String> = HashSet(6)

    /**
     * Adds the primitive's country code to known countries, by calculating the center of the feature.
     *
     * @param primitive Currently checked object
     * @return Primitive's country. Can be inaccurate because of the internal dataset precision!
     */
    private fun addNearbyCountry(primitive: OsmPrimitive): String {
        var currentCountry: String? = null
        for (country in countryCodes) {
            if (country != "EU" && Territories.isIso3166Code(country, primitive.bBox.center)) {
                // TODO: check if object spans across border
                nearbyCountryCodes.add(country)
                currentCountry = country
                break
            }
        }
        return currentCountry.toString()
    }

    /**
     * Country code of the given primitive.
     *
     * @param primitive Currently checked object
     * @return The ISO3166 code which the given OSM primitive center resides.
     */
    fun getIso3166Code(primitive: OsmPrimitive): String {
        var currentCountry: String? = null
        for (country in nearbyCountryCodes) {
            if (Territories.isIso3166Code(country, primitive.bBox.center)) {
                currentCountry = country
                break
            }
        }

        return when (currentCountry) {
            null -> addNearbyCountry(primitive)
            else -> currentCountry
        }
    }

    /**
     * Clears cached collected county codes. It is safe to call anytime.
     */
    fun clear() {
        nearbyCountryCodes.clear()
    }
}
