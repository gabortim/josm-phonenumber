package com.github.gabortim.phonenumber.tool

import com.github.gabortim.phonenumber.tool.NumberTools.containsDDI
import com.github.gabortim.phonenumber.tool.NumberTools.containsNonstandardChars
import com.github.gabortim.phonenumber.tool.NumberTools.splitAndStrip
import com.github.gabortim.phonenumber.tool.NumberTools.splitByLastSeparator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class NumberToolsTest {
    @Test
    fun testContainsNonstandardChars() {
        assertFalse(containsNonstandardChars("+43 50 5333-6630"))
        assertTrue(containsNonstandardChars("+36 30 DUGULAS"))
    }

    @Test
    @Disabled("Until a better DDI heuristic found")
    fun testContainsDDI() {
        // False
        assertFalse(containsDDI("+49 7751 3324"))
        assertFalse(containsDDI("(62)426483"))
        assertFalse(containsDDI("+49 7525 60150"))
        assertFalse(containsDDI("+41 56 624 20 04"))
        // FIXME: re-enable test with a better DDI heuristic
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
        assertEquals(numbersRef, numbers)

        numbers = splitAndStrip("+36 90 317 282, +36 90 317 457 ")
        numbersRef = arrayListOf("+36 90 317 282", "+36 90 317 457")
        assertEquals(numbersRef, numbers)

        numbers = splitAndStrip("(+36-1) 465-2010; (+36-1) 465-2016; (+36-1) 465-2017;")
        numbersRef = arrayListOf("(+36-1) 465-2010", "(+36-1) 465-2016", "(+36-1) 465-2017")
        assertEquals(numbersRef, numbers)
    }

    @Test
    fun testSplitByLastSeparator() {
        var processed = splitByLastSeparator("+36 90 317 282/256")
        assertArrayEquals(arrayOf("+36 90 317 282", "256", "/"), processed)
        processed = splitByLastSeparator("+36 62 474 255 / 14")
        assertArrayEquals(arrayOf("+36 62 474 255", "14", "/"), processed)
        processed = splitByLastSeparator("+36 62 474 255/14")
        assertArrayEquals(arrayOf("+36 62 474 255", "14", "/"), processed)
        processed = splitByLastSeparator("+3662474255/14")
        assertArrayEquals(arrayOf("+3662474255", "14", "/"), processed)
        processed = splitByLastSeparator("+36 62 544-310 / 301")
        assertArrayEquals(arrayOf("+36 62 544-310", "301", "/"), processed)
        processed = splitByLastSeparator("+3613251100/57311")
        assertArrayEquals(arrayOf("+3613251100", "57311", "/"), processed)
        processed = splitByLastSeparator("+3613251100#57311")
        assertArrayEquals(arrayOf("+3613251100", "57311", "#"), processed)
        processed = splitByLastSeparator("+43 5572 27505 723")
        assertArrayEquals(arrayOf("+43 5572 27505", "723", " "), processed)
        processed = splitByLastSeparator("+43 5574 46467 87090")
        assertArrayEquals(arrayOf("+43 5574 46467", "87090", " "), processed)
        processed = splitByLastSeparator("+43 5574 64999 64")
        assertArrayEquals(arrayOf("+43 5574 64999", "64", " "), processed)
        processed = splitByLastSeparator("+43 5574 45127 12510")
        assertArrayEquals(arrayOf("+43 5574 45127", "12510", " "), processed)
        processed = splitByLastSeparator("+36 62/424-805 / 612")
        assertArrayEquals(arrayOf("+36 62/424-805", "612", "/"), processed)
        processed = splitByLastSeparator("+43 50 5333-6630")
        assertArrayEquals(arrayOf("+43 50 5333", "6630", "-"), processed)
        processed = splitByLastSeparator("+43 5574 48442-0")
        assertArrayEquals(arrayOf("+43 5574 48442", "0", "-"), processed)
        processed = splitByLastSeparator("+4975312841557")
        assertArrayEquals(arrayOf("+4975312841557"), processed)
    }
}