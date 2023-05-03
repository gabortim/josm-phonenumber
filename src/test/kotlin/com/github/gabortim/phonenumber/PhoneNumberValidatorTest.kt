package com.github.gabortim.phonenumber

import com.github.gabortim.phonenumber.test.PhoneNumberValidator
import org.openstreetmap.josm.data.osm.*
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Ignore
import org.testng.annotations.Test


@Test
class PhoneNumberValidatorTest {
    companion object {
        private lateinit var validator: PhoneNumberValidator
        private var ds: DataSet? = null
    }

    @BeforeTest
    fun setUp() {
        validator = PhoneNumberValidator()
        ds = DataSet()
//        MainApplication.getLayerManager().addLayer(OsmDataLayer(ds, null, null))
    }

    @AfterTest
    fun tearDown() {
    }

    @Test
    @Ignore
    fun testIsPrimitiveUsable() {
        var primitive: OsmPrimitive = Node()
        val tags = TagMap("phone", "+36 66 390 686")
        primitive.setKeys(tags)
        ds?.addPrimitive(primitive)

        assertTrue(validator.isPrimitiveUsable(primitive))

        primitive = Way()
        primitive.setKeys(tags)
        assertTrue(validator.isPrimitiveUsable(primitive))

        primitive = Relation()
        primitive.setKeys(tags)
        assertTrue(validator.isPrimitiveUsable(primitive))

        primitive = Node()
        primitive.setKeys(TagMap("contact:phone_2", "+36 66 390 686"))
        assertTrue(validator.isPrimitiveUsable(primitive))
    }

    @Test
    @Ignore
    fun testFixError() {
        val primitive = Node()
        val tags = TagMap("phone", "+36 66 390 686")
        primitive.setKeys(tags)

        validator.expertChanged(true)
        validator.check(primitive)

        for (error in validator.errors) {
            assertTrue(error.isFixable)
            error.fix
            assertEquals(primitive, Node().setKeys(TagMap("contact:phone", "+36 66 390 686")))
        }
    }

    @Test
    @Ignore
    fun testIsFixable() {
    }
}