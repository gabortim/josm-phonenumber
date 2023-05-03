package com.github.gabortim.phonenumber.tool


/**
 * This class contains various regex based methods.
 */
internal object NumberTools {
    /**
     * Returns true if the input string contains anything that shouldn't be in the phone number.
     *
     * @param value OSM value to be checked
     * @return Bool value if it contains those characters.
     */
    fun containsNonstandardChars(value: String): Boolean {
        return value.contains(Regex("[^-/+0-9;() #]+")) && !value.contains("ext.")
    }

    /**
     * Checks the number if it contains a direct dial-in (DDI) number extension (PBX extension).
     *
     * @param value OSM value to be checked
     * @return Boole value if it contains direct dial in number (DDI)
     */
    fun containsDDI(value: String): Boolean {
        return value.contains(Regex("\\+?([0-9- ]){6,}[-/#][0-9 ]+\$"))
    }

    /**
     * Splits the phone number by any of the possible direct dial-in (DDI) number separators.
     *
     * @return Split number. First value is the number, second is the extension, last one is the original separator.
     * If couldn't return, the input parameter returned in a String array.
     */
    fun splitByLastSeparator(number: String): Array<String> {
        var splitNum = number.split(Regex("[-/#](?!.*[-/#])"))

        if (splitNum.size < 2) {
            // if couldn't split by regular separators, split by last space char
            // needed for cases, like " / " used as a separator
            splitNum = number.split(Regex(" (?!.* )"))
        }

        if (splitNum.size < 2)
            return arrayOf(number)

        val usedSeparator = number[splitNum[0].length].toString()

        return arrayOf(splitNum[0].trim(), splitNum[1].trim(), usedSeparator)
    }

    /**
     * Splits the values by comma or semicolon. After splitting, removes leading and trailing whitespaces.
     *
     * @param value string to split
     * @return Split string in an array
     */
    fun splitAndStrip(value: String): Collection<String> {
        return value.split(Regex("[,;]"))
            .filter(String::isNotBlank)
            .map(String::trim)
    }
}