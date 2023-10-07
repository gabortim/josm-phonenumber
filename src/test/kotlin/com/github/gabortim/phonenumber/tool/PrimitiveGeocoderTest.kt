package com.github.gabortim.phonenumber.tool

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.testutils.annotations.Territories

@Territories
class PrimitiveGeocoderTest {
    @BeforeEach
    fun setUp() {
        PrimitiveGeocoder.clear()
    }

    @Test
    fun testGetIso3166Alpha2Code() {
        val latLonHungary = LatLon(46.7079, 19.3799)   // somewhere in Hungary
        val primitive = Node(latLonHungary)

        var isoCode = PrimitiveGeocoder.getIso3166Alpha2Code(primitive)
        assertEquals("HU", isoCode)

        // call it once more to trigger cache retrieval
        isoCode = PrimitiveGeocoder.getIso3166Alpha2Code(primitive)
        assertEquals("HU", isoCode)
    }

    @Test
    fun testGetIso3166Alpha2Code_github7() {
        val latLonTennessee = LatLon(35.7264, -86.2537)   // somewhere Tennessee, US
        val primitive = Node(latLonTennessee)

        val isoCode = PrimitiveGeocoder.getIso3166Alpha2Code(primitive)
        assertEquals("US", isoCode)
    }

    @Test
    fun testClear() {
        val latLonHungary = LatLon(46.7079, 19.3799)   // somewhere in Hungary
        val primitive = Node(latLonHungary)

        val isoCode = PrimitiveGeocoder.getIso3166Alpha2Code(primitive)
        assertEquals("HU", isoCode)

        val cache = PrimitiveGeocoder.javaClass.getDeclaredField("nearbyCountryCodes")
        cache.isAccessible = true

        assertEquals(1, (cache.get(PrimitiveGeocoder) as HashSet<*>).size)

        PrimitiveGeocoder.clear()

        assertEquals(0, (cache.get(PrimitiveGeocoder) as HashSet<*>).size)
    }
}