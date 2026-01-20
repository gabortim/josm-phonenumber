package com.github.gabortim.phonenumber.validation

import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun testPremiumNumber() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 90 317 282")
        node.setKeys(tags)

        val pn = PhoneNumber(node, "HU", false)
        assertTrue(pn.isFixable())
        assertTrue(pn.getValidatorDescription().contains("premium rate number"))
    }

    @Test
    fun testWrongSeparator() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 1 123 4567, +36 1 123 4568")
        node.setKeys(tags)

        val pn = PhoneNumber(node, "HU", false)
        assertTrue(pn.isFixable())
        assertTrue(pn.badSeparator.contains("phone"))
    }

    @Test
    fun testSchemaChange() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 1 123 4567")
        node.setKeys(tags)

        val pn = PhoneNumber(node, "HU", true)
        assertTrue(pn.isFixable())
        assertTrue(pn.getValidatorDescription().contains("contact: prefix scheme recommended"))
    }

    @Test
    fun testInappropriateKey() {
        val node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 70 123 4567") // This is a mobile number in HU
        node.setKeys(tags)

        val pn = PhoneNumber(node, "HU", false)
        assertTrue(pn.isFixable())
        assertTrue(pn.getValidatorDescription().contains("inappropriate key"))
        assertEquals("+36 70 123 4567", pn.getAsMap()["contact:mobile"])
        assertEquals("", pn.getAsMap()["phone"])
    }
}