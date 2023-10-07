package com.github.gabortim.phonenumber.test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.*
import org.openstreetmap.josm.data.preferences.BooleanProperty
import org.openstreetmap.josm.data.validation.Severity
import org.openstreetmap.josm.testutils.annotations.BasicPreferences
import org.openstreetmap.josm.testutils.annotations.Territories


@BasicPreferences
@Territories
class PhoneNumberValidatorTest {
    companion object {
        private lateinit var validator: PhoneNumberValidator
        private lateinit var ds: DataSet

        private val latLonHungary = LatLon(46.7079, 19.3799)   // somewhere in Hungary
    }

    @BeforeEach
    fun setUp() {
        validator = PhoneNumberValidator()
        ds = DataSet()
    }

    @Test
    fun testIsPrimitiveUsable() {
        var node = Node(LatLon.ZERO)
        val tags = TagMap("phone", "+36 66 390 686")
        node.setKeys(tags)

        assertTrue(validator.isPrimitiveUsable(node))

        val way = Way()
        way.setKeys(tags)

        // BBox invalid test should fail
        assertFalse(validator.isPrimitiveUsable(way))

        way.addNode(node)
        way.addNode(Node(LatLon(0.1, 0.1)))

        // BBox valid test should pass
        assertTrue(validator.isPrimitiveUsable(way))

        val relation = Relation()
        relation.setKeys(tags)

        // BBox invalid test should fail
        assertFalse(validator.isPrimitiveUsable(relation))

        relation.addMember(RelationMember("", way))

        // BBox valid test should pass
        assertTrue(validator.isPrimitiveUsable(relation))

        node = Node(LatLon.ZERO)
        node.setKeys(TagMap("contact:phone_2", "+36 66 390 686"))
        assertTrue(validator.isPrimitiveUsable(node))

        node.setKeys(TagMap("2_contact:phone", "+36 66 390 686"))
        assertFalse(validator.isPrimitiveUsable(node))
    }

    @Test
    fun testFixError() {
        val primitive = Node(latLonHungary)
        ds.addPrimitive(primitive)
        val tags = TagMap("phone", "+36 66 390 686")
        primitive.setKeys(tags)

        setAutofixProperty(true)

        validator.check(primitive)
        for (error in validator.errors) {
            assertTrue(error.isFixable)
            error.fix.executeCommand()

            val node = Node(latLonHungary)
            node.setKeys(TagMap("contact:phone", "+36 66 390 686"))

            assertEquals(node.keys, primitive.keys)
        }
    }

    @Test
    fun testIsFixable() {
        val primitive = Node(latLonHungary)
        ds.addPrimitive(primitive)
        val tags = TagMap("phone", "+36 66 390 686")
        primitive.setKeys(tags)

        validator.check(primitive)

        setAutofixProperty(false)
        assertFalse(validator.isFixable(validator.errors[0]))

        setAutofixProperty(true)
        assertTrue(validator.isFixable(validator.errors[0]))
    }

    @Test
    fun testErrorRaiseOnError() {
        val node = Node(latLonHungary)
        val tags = TagMap("phone", "+36 70 941544")     // error in the value
        node.setKeys(tags)
        ds.addPrimitive(node)

        validator.check(node)
        assertEquals(1, validator.errors.size)
    }

    @Test
    fun testRaiseWarningBeautifiable() {
        val node = Node(latLonHungary)
        val tags = TagMap("contact:mobile", "+36 70 000 0000 ; +36 70 000 0001")    // extra char around the sep
        node.setKeys(tags)
        ds.addPrimitive(node)

        validator.check(node)
        assertEquals(1, validator.errors.size)
        assertEquals("beautifiable value", validator.errors[0].description)
    }

    @Test
    fun testDoubleContactPrefixIssue() {
        val node = Node(latLonHungary)
        val tags = TagMap("contact:mobile", "+36 (70) 000 0000; some extra /value")
        node.setKeys(tags)
        ds.addPrimitive(node)

        setAutofixProperty(true)
        validator.check(node)
        validator.errors
            .stream()
            .filter { error -> error.severity.equals(Severity.WARNING) }
            .forEach{ testError -> testError.fix.executeCommand() }

        ds.allPrimitives().forEach { _ ->
            val nodeRef = Node(latLonHungary)
            val tagsRef = TagMap("contact:mobile", "+36 70 000 0000;some extra /value")
            nodeRef.setKeys(tagsRef)

            assertEquals(nodeRef.keys, node.keys)
        }
    }

    @Test
    fun testRaiseWarningInWrongRegion() {
        val latLonSlovenia = LatLon(46.225, 15.079)
        val node = Node(latLonSlovenia)
        val tags = TagMap("contact:mobile", "+36 70 000 0000")
        node.setKeys(tags)
        ds.addPrimitive(node)

        validator.check(node)
        assertEquals(1, validator.errors.size)
        assertTrue("region" in validator.errors[0].message)
    }

    @Test
    fun testRaiseWarningBadSeparator() {
        val node = Node(latLonHungary)
        val tags = TagMap("contact:mobile", "+36 70 000 0000,+36 70 000 0001")
        node.setKeys(tags)
        ds.addPrimitive(node)

        validator.check(node)
        assertEquals(1, validator.errors.size)
        assertTrue("separator" in validator.errors[0].description)
    }

    private fun setAutofixProperty(value: Boolean) {
        // use reflection to set autofix to true
        val autofix = validator.javaClass.getDeclaredField("autofixProperty")
        autofix.isAccessible = true
        (autofix.get(validator) as BooleanProperty).put(value)
    }
}