package com.github.gabortim.phonenumber.tool

import org.openstreetmap.josm.tools.PatternUtils


/**
 * This class contains various regex based methods.
 */
internal object NumberTools {
    /**
     * Returns true if the input string contains characters not typically found in phone numbers.
     *
     * @param value OSM value to be checked
     * @return `true` if it contains non-standard characters.
     */
    fun containsNonstandardChars(value: String): Boolean {
        return value.contains(PatternUtils.compile("[^-/+0-9;() #]+").toRegex()) && !value.contains("ext.")
    }

    /**
     * Checks if the number contains a direct dial-in (DDI) extension.
     *
     * @param value OSM value to be checked
     * @return `true` if it contains a DDI extension
     */
    fun containsDDI(value: String): Boolean {
        return value.contains(PatternUtils.compile("\\+?([0-9- ]){6,}[-/#][0-9 ]+\$").toRegex())
    }

    /**
     * Splits the phone number by the last direct dial-in (DDI) separator.
     *
     * @return A list containing the number, the extension, and the used separator.
     * If splitting fails, returns a list containing only the original number.
     */
    fun splitByLastSeparator(number: String): List<String> {
        var splitNum = number.split(PatternUtils.compile("[-/#](?!.*[-/#])").toRegex())

        if (splitNum.size < 2) {
            // if couldn't split by regular separators, split by last space char
            // needed for cases like " / " used as a separator
            splitNum = number.split(PatternUtils.compile(" (?!.* )").toRegex())
        }

        if (splitNum.size < 2) {
            return listOf(number)
        }

        val usedSeparator = number[splitNum[0].length].toString()

        return listOf(splitNum[0].trim(), splitNum[1].trim(), usedSeparator)
    }

    /**
     * Splits the values by comma or semicolon. After splitting, removes leading and trailing whitespaces.
     *
     * @param value string to split
     * @return Split strings in a collection
     */
    fun splitAndStrip(value: String): Collection<String> {
        return value.split(PatternUtils.compile("[,;]").toRegex())
            .filter(String::isNotBlank)
            .map(String::trim)
    }
}