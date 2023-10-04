package com.github.gabortim.phonenumber.tool

import com.github.gabortim.phonenumber.tool.NumberTools.containsDDI
import com.github.gabortim.phonenumber.tool.NumberTools.containsNonstandardChars
import com.github.gabortim.phonenumber.tool.NumberTools.splitByLastSeparator
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.ValidationResult
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.i18n.phonenumbers.ShortNumberInfo

internal object NumberFormatter {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val shortNumberInfo = ShortNumberInfo.getInstance()

    enum class FailReason {
        /** No issues found. */
        NONE,

        /** Contains unusual chars. */
        UNUSUAL_CHARS,

        /** Parse error. */
        INVALID,

        /** The number is too short. */
        TOO_SHORT,

        /** The number is associated with premium rate service. */
        PREMIUM,

        /** The number of grouping characters is unusual. This error is not stable yet. */
        NOT_WELL_FORMATTED;
    }

    /**
     * Formats the phone number into international ITU-T Rec. E.123 format.
     *
     * @param phoneNumber Input number
     * @return A pair consist of the prettified phone number and the reason if the formatting failed
     */
    fun format(phoneNumber: PhoneNumber): Pair<String, FailReason> {
        if (containsNonstandardChars(phoneNumber.rawInput)) {
            // libphonenumber can accept format "+36 30 DUGULAS" as a valid format,
            // but deny those instead.
            return Pair("", FailReason.UNUSUAL_CHARS)
        }

        // SHORT NUMBER VALIDATION
        if (isValidShort(phoneNumber) || isEmergency(phoneNumber)) {
            return Pair(
                phoneNumberUtil.formatInOriginalFormat(phoneNumber, phoneNumber.countryCodeSource.name),
                FailReason.NONE
            )
        }

        // Fast number length check
        if (phoneNumberUtil.isPossibleNumberWithReason(phoneNumber) == ValidationResult.TOO_SHORT) {
            return Pair("", FailReason.TOO_SHORT)
        }

        // PREMIUM NUMBER CHECKING
        if (isPremiumRate(phoneNumber)) {
            return Pair("", FailReason.PREMIUM)
        }

        // DDI EXTENSION CHECKING
        if (containsDDI(phoneNumber.rawInput) && possiblyValidExtensionSeparatorUsed(phoneNumber.rawInput)) {
            val numbers = splitByLastSeparator(phoneNumber.rawInput)
            val parsedAgain: PhoneNumber
            try {
                parsedAgain = phoneNumberUtil.parse(numbers[0], getRegionCode(phoneNumber))
            } catch (e: NumberParseException) {
                return Pair("", FailReason.INVALID)
            }

            // add default "ext." as separator
            parsedAgain.extension = numbers[1]
            if (isValid(parsedAgain)) {
                return Pair(formatNumber(parsedAgain), FailReason.NONE)
            }
        }

        // FULL VALIDATION
        if (!isValid(phoneNumber)) {
            return Pair("", FailReason.INVALID)
        }
        val formatted = formatNumber(phoneNumber)

        // FINAL GROUPING CHECK
        if (!hasEnoughGroupingChars(phoneNumber, formatted)) {
            return Pair("", FailReason.NOT_WELL_FORMATTED)
        }

        // RETURN FORMATTED, NO (SERIOUS) ISSUE FOUND
        return Pair(formatted, FailReason.NONE)
    }

    /**
     * Returns if enough grouping characters found in the formatted string.
     */
    private fun hasEnoughGroupingChars(original: PhoneNumber, formatted: String): Boolean {
        val numberOfOriginalGroupingChars = getGroupingCharsCount(original.rawInput)
        val numberOfFormattedGroupingChars = getGroupingCharsCount(formatted)

        // TODO: if doesn't have enough separator chars, use the parameter
        //int stripStringLen = formatted.length() - numberOfFormattedGroupingChars;
        return if (numberOfFormattedGroupingChars < numberOfOriginalGroupingChars &&
            formatted.length / numberOfFormattedGroupingChars < 3
        ) {
            false
        } else true

//        final int minimumSpace;
//
//        switch (phoneNumberUtil.getRegionCodeForNumber(phoneNumber)) {
//            case "AT":
//            case "DE":
//                minimumSpace = 2;
//                break;
//            default:
//                minimumSpace = 3;
//        }
    }

    /**
     * Fast implementation for checking number of grouping characters.
     */
    private fun getGroupingCharsCount(number: String): Int {
        // TODO: check "ext." too!
        return number.count { it == ' ' || it == '#' || it == '-' || it == '/' }
    }

    /**
     * Checks if the last grouping char used only once. This makes a possible valid separation,
     * although invalid for the parser.
     */
    fun possiblyValidExtensionSeparatorUsed(number: String): Boolean {
        val array = splitByLastSeparator(number)
        if (array.size < 3) return false

        val sep: Char = array[2][0]
        val sepCount = number.count { it == sep }

        // && part: make sure more than 1 separator used
        return (sepCount == 1 || sepCount == 2) && getGroupingCharsCount(number) > 1
    }

    /**
     * Formats the phone number to international format.
     *
     * Ensures that everywhere the same formatting is used.
     */
    private fun formatNumber(phoneNumber: PhoneNumber): String {
        return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    }

    /**
     * Returns the region code for the specified number.
     */
    private fun getRegionCode(phoneNumber: PhoneNumber): String? {
        return phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
    }

    fun isValid(phoneNumber: PhoneNumber): Boolean {
        return phoneNumberUtil.isValidNumber(phoneNumber)
    }

    fun isValidShort(phoneNumber: PhoneNumber): Boolean {
        return shortNumberInfo.isValidShortNumber(phoneNumber)
    }

    fun isPremiumRate(phoneNumber: PhoneNumber): Boolean {
        return phoneNumberUtil.getNumberType(phoneNumber) == PhoneNumberUtil.PhoneNumberType.PREMIUM_RATE
    }

    fun isEmergency(phoneNumber: PhoneNumber): Boolean {
        return shortNumberInfo.isEmergencyNumber(
            phoneNumber.rawInput,
            phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
        )
    }
}