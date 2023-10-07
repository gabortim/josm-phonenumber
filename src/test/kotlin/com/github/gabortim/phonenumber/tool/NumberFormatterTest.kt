package com.github.gabortim.phonenumber.tool

import com.github.gabortim.phonenumber.tool.NumberFormatter.format
import com.github.gabortim.phonenumber.tool.NumberFormatter.isEmergency
import com.github.gabortim.phonenumber.tool.NumberFormatter.isPremiumRate
import com.github.gabortim.phonenumber.tool.NumberFormatter.possiblyValidExtensionSeparatorUsed
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.testutils.annotations.BasicPreferences


@BasicPreferences
class NumberFormatterTest {
    companion object {
        private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    }

    @Test
    fun testFormatNumber_AT() {
        val region = "AT"
        try {
            var number = util.parseAndKeepRawInput("+43 50 5333-6630", region)
            assertEquals("+43 50 5333 ext. 6630", format(number).first)
            number = util.parseAndKeepRawInput("+43 5675 6444-26", region)
            assertEquals("+43 5675 6444 ext. 26", format(number).first)
            number = util.parseAndKeepRawInput("+43 5558 8213-410", region)
            assertEquals("+43 5558 8213 ext. 410", format(number).first)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testFormatNumber_DE() {
        val region = "DE"
        try {
            val number = util.parseAndKeepRawInput("+49 771 898649-9", region)
            assertEquals("+49 771 898649 ext. 9", format(number).first)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testFormatNumber_HU() {
        val region = "HU"
        try {
            var number = util.parseAndKeepRawInput("+36 62 800800", region)
            assertEquals("+36 62 800 800", format(number!!).first)

            number = util.parseAndKeepRawInput("+3662488446", region)
            assertEquals("+36 62 488 446", format(number).first)

            number = util.parseAndKeepRawInput("(62)426483", region)
            assertEquals("+36 62 426 483", format(number).first)

            number = util.parseAndKeepRawInput("+36209209000", region)
            assertEquals("+36 20 920 9000", format(number).first)

            number = util.parseAndKeepRawInput("+36209209000", region)
            assertEquals("+36 20 920 9000", format(number).first)

            number = util.parseAndKeepRawInput("+36 62/424-805 / 612", region)
            assertEquals("+36 62 424 805 ext. 612", format(number).first)

            number = util.parseAndKeepRawInput("+36 62/424-805 # 612", region)
            assertEquals("+36 62 424 805 ext. 612", format(number).first)

            number = util.parseAndKeepRawInput("+36 62/424-805 - 612", region)
            assertEquals("+36 62 424 805 ext. 612", format(number).first)

            number = util.parseAndKeepRawInput("+36 1 3408132", region)
            assertEquals("+36 1 340 8132", format(number).first)

            number = util.parseAndKeepRawInput("1272", region)
            assertEquals("1272", format(number).first)

        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail(e.message)
        }
    }

    @Test
    fun testFormatNumber_US() {
        val region = "US"
        try {
            var number = util.parseAndKeepRawInput("931-647-1567", region)
            assertEquals("+1 931-647-1567", format(number).first)

            number = util.parseAndKeepRawInput("1-615-371-9200", region)
            assertEquals("+1 615-371-9200", format(number).first)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testNoFormatNumber() {
        val region = "HU"
        val s = "(Polgármester: Lehoczki Szabolcs, 2019- )"
        val number = util.parseAndKeepRawInput(s, region)
        assertEquals("", format(number).first)
    }

    @Test
    @Disabled
    fun testGetGroupingCharsCount() {
    }

    @Test
    @Disabled
    fun testIsValid() {
    }

    @Test
    @Disabled
    fun testIsValidShort() {
    }

    @Test
    fun testIsPremiumRateNumber() {
        val region = "HU"
        try {
            var number = util.parseAndKeepRawInput("+36 90 317 282", region)
            assertTrue(isPremiumRate(number!!))
            number = util.parseAndKeepRawInput("+36 91 500 316", region)
            assertTrue(isPremiumRate(number))
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testIsEmergencyNumber() {
        val region = "HU"
        val number: PhoneNumber
        try {
            number = util.parseAndKeepRawInput("112", region)
            assertTrue(isEmergency(number))
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testPossiblyValidExtensionSeparatorUsed() {
        assertTrue(possiblyValidExtensionSeparatorUsed("+36 90 317 282/256"))

        assertFalse(possiblyValidExtensionSeparatorUsed("+497565 1432"))
    }
}