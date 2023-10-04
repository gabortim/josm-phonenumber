package com.github.gabortim.phonenumber.tool

import com.github.gabortim.phonenumber.tool.NumberTools.containsDDI
import com.github.gabortim.phonenumber.tool.NumberTools.containsNonstandardChars
import com.github.gabortim.phonenumber.tool.NumberTools.splitAndStrip
import com.github.gabortim.phonenumber.tool.NumberTools.splitByLastSeparator
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class NumberToolsTest {
    @Test
    fun testContainsNonstandardChars() {
        assertFalse(containsNonstandardChars("+43 50 5333-6630"))
        assertTrue(containsNonstandardChars("+36 30 DUGULAS"))
    }

    @Test(enabled = false)
    fun testContainsDDI() {
        // False
        assertFalse(containsDDI("+49 7751 3324"))
        assertFalse(containsDDI("(62)426483"))
        assertFalse(containsDDI("+49 7525 60150"))
        assertFalse(containsDDI("+41 56 624 20 04"))
        assertFalse(containsDDI("+36 30 / 633 0961"))

        // True
        assertTrue(containsDDI("+36 90 317 282/256"))
        assertTrue(containsDDI("+43 5574 87278 88"))
        assertTrue(containsDDI("+43 5572 27505 723"))
        assertTrue(containsDDI("+43 5574 46467 87090"))
        assertTrue(containsDDI("+43 5574 64999 64"))
    }

    @Test
    fun testSplitAndStrip() {
        var numbers = splitAndStrip("+36 90 317 282")
        var numbersRef = arrayListOf("+36 90 317 282")
        assertEquals(numbers, numbersRef)

        numbers = splitAndStrip("+36 90 317 282, +36 90 317 457 ")
        numbersRef = arrayListOf("+36 90 317 282", "+36 90 317 457")
        assertEquals(numbers, numbersRef)

        numbers = splitAndStrip("(+36-1) 465-2010; (+36-1) 465-2016; (+36-1) 465-2017;")
        numbersRef = arrayListOf("(+36-1) 465-2010", "(+36-1) 465-2016", "(+36-1) 465-2017")
        assertEquals(numbers, numbersRef)
    }

    @Test
    fun testSplitByLastSeparator() {
        var processed = splitByLastSeparator("+36 90 317 282/256")
        assertEquals(processed, arrayOf("+36 90 317 282", "256", "/"))
        processed = splitByLastSeparator("+36 62 474 255 / 14")
        assertEquals(processed, arrayOf("+36 62 474 255", "14", "/"))
        processed = splitByLastSeparator("+36 62 474 255/14")
        assertEquals(processed, arrayOf("+36 62 474 255", "14", "/"))
        processed = splitByLastSeparator("+3662474255/14")
        assertEquals(processed, arrayOf("+3662474255", "14", "/"))
        processed = splitByLastSeparator("+36 62 544-310 / 301")
        assertEquals(processed, arrayOf("+36 62 544-310", "301", "/"))
        processed = splitByLastSeparator("+3613251100/57311")
        assertEquals(processed, arrayOf("+3613251100", "57311", "/"))
        processed = splitByLastSeparator("+3613251100#57311")
        assertEquals(processed, arrayOf("+3613251100", "57311", "#"))
        processed = splitByLastSeparator("+43 5572 27505 723")
        assertEquals(processed, arrayOf("+43 5572 27505", "723", " "))
        processed = splitByLastSeparator("+43 5574 46467 87090")
        assertEquals(processed, arrayOf("+43 5574 46467", "87090", " "))
        processed = splitByLastSeparator("+43 5574 64999 64")
        assertEquals(processed, arrayOf("+43 5574 64999", "64", " "))
        processed = splitByLastSeparator("+43 5574 45127 12510")
        assertEquals(processed, arrayOf("+43 5574 45127", "12510", " "))
        processed = splitByLastSeparator("+36 62/424-805 / 612")
        assertEquals(processed, arrayOf("+36 62/424-805", "612", "/"))
        processed = splitByLastSeparator("+43 50 5333-6630")
        assertEquals(processed, arrayOf("+43 50 5333", "6630", "-"))
        processed = splitByLastSeparator("+43 5574 48442-0")
        assertEquals(processed, arrayOf("+43 5574 48442", "0", "-"))
        processed = splitByLastSeparator("+4975312841557")
        assertEquals(processed, arrayOf("+4975312841557"))
    }
}