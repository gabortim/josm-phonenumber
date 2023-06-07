package com.github.gabortim.phonenumber

import com.github.gabortim.phonenumber.tool.NumberFormatter.format
import com.github.gabortim.phonenumber.tool.NumberFormatter.isEmergency
import com.github.gabortim.phonenumber.tool.NumberFormatter.isPremiumRate
import com.github.gabortim.phonenumber.tool.NumberFormatter.possiblyValidExtensionSeparatorUsed
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NumberFormatterTest {
    companion object {
        private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    }

    @BeforeMethod
    fun setUp() {
    }

    @AfterMethod
    fun tearDown() {
    }

    @Test
    fun testFormatNumber_AT() {
        val region = "AT"
        try {
            var number = phoneNumberUtil.parseAndKeepRawInput("+43 50 5333-6630", region)
            assertEquals(format(number).first, "+43 50 5333 ext. 6630")
            number = phoneNumberUtil.parseAndKeepRawInput("+43 5675 6444-26", region)
            assertEquals(format(number).first, "+43 5675 6444 ext. 26")
            number = phoneNumberUtil.parseAndKeepRawInput("+43 5558 8213-410", region)
            assertEquals(format(number).first, "+43 5558 8213 ext. 410")
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testFormatNumber_DE() {
        val region = "DE"
        try {
            val number = phoneNumberUtil.parseAndKeepRawInput("+49 771 898649-9", region)
            assertEquals(format(number).first, "+49 771 898649 ext. 9")
        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail()
        }
    }

    @Test
    fun testFormatNumber_HU() {
        val region = "HU"
        try {
            var number = phoneNumberUtil.parseAndKeepRawInput("+36 62 800800", region)
            assertEquals(format(number!!).first, "+36 62 800 800")

            number = phoneNumberUtil.parseAndKeepRawInput("+3662488446", region)
            assertEquals(format(number).first, "+36 62 488 446")

            number = phoneNumberUtil.parseAndKeepRawInput("(62)426483", region)
            assertEquals(format(number).first, "+36 62 426 483")

            number = phoneNumberUtil.parseAndKeepRawInput("+36209209000", region)
            assertEquals(format(number).first, "+36 20 920 9000")

            number = phoneNumberUtil.parseAndKeepRawInput("+36209209000", region)
            assertEquals(format(number).first, "+36 20 920 9000")

            number = phoneNumberUtil.parseAndKeepRawInput("+36 62/424-805 / 612", region)
            assertEquals(format(number).first, "+36 62 424 805 ext. 612")

            number = phoneNumberUtil.parseAndKeepRawInput("+36 1 3408132", region)
            assertEquals(format(number).first, "+36 1 340 8132")

            number = phoneNumberUtil.parseAndKeepRawInput("1272", region)
            assertEquals(format(number).first, "1272")

        } catch (e: NumberParseException) {
            e.printStackTrace()
            fail(e.message)
        }
    }

    @Test
    @Throws(NumberParseException::class)
    fun testNoFormatNumber() {
        val region = "HU"
        val s = "(Polgármester: Lehoczki Szabolcs, 2019- )"
        val number = phoneNumberUtil.parseAndKeepRawInput(s, region)
        format(number)
    }

    @Test(enabled = false)
    fun testGetGroupingCharsCount() {
    }

    @Test(enabled = false)
    fun testIsValid() {
    }

    @Test(enabled = false)
    fun testIsValidShort() {
    }

    @Test
    fun testIsPremiumRateNumber() {
        val region = "HU"
        try {
            var number = phoneNumberUtil.parseAndKeepRawInput("+36 90 317 282", region)
            assertTrue(isPremiumRate(number!!))
            number = phoneNumberUtil.parseAndKeepRawInput("+36 91 500 316", region)
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
            number = phoneNumberUtil.parseAndKeepRawInput("112", region)
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