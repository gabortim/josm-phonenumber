package com.github.gabortim.phonenumber.test

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.TagMap
import org.openstreetmap.josm.testutils.annotations.BasicPreferences

@BasicPreferences
/**
 * Test for PhoneNumber.
 */
class PhoneNumberTest {
    @Test
    fun isFixable() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 66 390 686")
        node.setKeys(tags)

        assertFalse(PhoneNumber(node, "HU", false).isFixable())
        assertTrue(PhoneNumber(node, "HU", true).isFixable())
    }

    @Test
    fun testDuplicateDetection() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("contact:mobile", "+36 30 000 0000;+36 30 000 0000")
        node.setKeys(tags)

        assertTrue(PhoneNumber(node, "HU", true).isFixable())
    }
}