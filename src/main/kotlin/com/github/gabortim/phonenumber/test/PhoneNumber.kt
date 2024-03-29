package com.github.gabortim.phonenumber.test

import com.github.gabortim.phonenumber.tool.NumberFormatter
import com.github.gabortim.phonenumber.tool.NumberFormatter.FailReason
import com.github.gabortim.phonenumber.tool.NumberTools.splitAndStrip
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import com.google.i18n.phonenumbers.Phonenumber
import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.data.osm.TagMap
import org.openstreetmap.josm.tools.I18n.tr
import org.openstreetmap.josm.tools.I18n.trn
import org.openstreetmap.josm.tools.Logging


/**
 * Key prefix used for contact tagging scheme
 */
const val CONTACT_SCHEME_PREFIX = "contact:"

/**
 * An object holding all phone tags of the given primitive.
 * Handles parsing, formatting and switching schemes.
 *
 * When formatting, automatically recognises the used tagging scheme and adopts it.
 * The newer contact: prefix scheme can be forced if needed.
 *
 * @param primitive OSM primitive to process
 * @param region Primitive's region
 * @param switchToContactScheme Determines phone tag switch to contact: prefix scheme
 */
class PhoneNumber(
    private val primitive: OsmPrimitive,
    private val region: String,
    private var switchToContactScheme: Boolean
) {
    /**
     * OSM primitive key prefix.
     */
    private var prefix = ""

    private val originalNumbers = HashMap<String, ArrayList<String>>(4)
    private val processedNumbers = HashMap<String, LinkedHashSet<String>>(4)

    /**
     * Container for badly separated tags. Contains tag keys.
     */
    val badSeparator by lazy { HashSet<String>(2) }

    /**
     * Container for numbers which are in the wrong region. Contains tag values.
     */
    val inWrongRegion by lazy { HashSet<String>(2) }

    /**
     * Container for found premium numbers. Contains tag values.
     */
    private val premiumNumber by lazy { HashSet<String>(2) }

    /**
     * Container for invalid numbers which libphonenumber couldn't parse. Contains tag values.
     */
    val invalid by lazy { HashSet<String>(2) }

    /**
     * Container for too short (invalid) numbers. Contains tag values.
     */
    val tooShort by lazy { HashSet<String>(2) }

    /**
     * Container for numbers with unusual characters found in them. Contains tag values.
     */
    val unusualChars by lazy { HashSet<String>(2) }

    /**
     * Container for not well-formatted numbers. Contains tag values.
     */
    val notWellFormatted by lazy { HashSet<String>(2) }

    /**
     * Set to true if any of the tags modified -> beautifyable.
     */
    private var isBeautifyable = false

    /**
     * Set to true if any of the values formatted.
     */
    private var isFormatted = false

    /**
     * True if any of the tags switched class e.g. phone -> mobile,
     * but ignoring the contact: prefix, e.g. phone -> contact:phone.
     */
    private var hasSwitchedClass = false

    /**
     * True if any of the tags switched to the contact: prefix
     * scheme, e.g. phone -> contact:phone.
     */
    private var hasSchemaChange = false

    init {
        splitTagValues()
        categorizeTags()
    }

    companion object {
        private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    }

    /**
     * Splits tag values into separate phone numbers and fills internal data structure. Used for preprocessing.
     */
    private fun splitTagValues() {
        for (tag in primitive.keys.filterKeys(PhoneNumberValidator.Companion::isKeyUsable)) {
            if (switchToContactScheme || CONTACT_SCHEME_PREFIX in tag.key)
                prefix = CONTACT_SCHEME_PREFIX

            if (tag.value.contains(','))
                badSeparator.add(tag.key)

            val splitValues = splitAndStrip(tag.value) as ArrayList<String>

            if (!tag.value.equals(splitValues.joinToString(SEP.toString()))) {
                isBeautifyable = true
            }

            originalNumbers[tag.key] = splitValues
        }
    }

    /**
     * Separate numbers into phone=* and mobile=* tags.
     */
    private fun categorizeTags() {
        val parsed: Phonenumber.PhoneNumber = Phonenumber.PhoneNumber()

        // iterate over values
        for (tag in originalNumbers.entries) {
            // iterate over keys
            for (number in tag.value) {
                try {
                    phoneNumberUtil.parseAndKeepRawInput(number, region, parsed)
                    val formatted = NumberFormatter.format(parsed)

                    if (region != phoneNumberUtil.getRegionCodeForNumber(parsed))
                        inWrongRegion.add(formatted.first)

                    when (formatted.second) {
                        FailReason.NONE -> {
                            if ("fax" in tag.key.lowercase())
                                addEntry("fax", formatted.first, tag.key)
                            else if (phoneNumberUtil.getNumberType(parsed) == PhoneNumberType.MOBILE)
                                addEntry("mobile", formatted.first, tag.key)
                            else
                                addEntry("phone", formatted.first, tag.key)

                            if (number != formatted.first)
                                isFormatted = true
                        }

                        FailReason.UNUSUAL_CHARS -> unusualChars.add(number)
                        FailReason.INVALID -> invalid.add(number)
                        FailReason.TOO_SHORT -> tooShort.add(number)
                        FailReason.PREMIUM -> premiumNumber.add(number)
                        FailReason.NOT_WELL_FORMATTED -> notWellFormatted.add(number)
                    }

                } catch (e: NumberParseException) {
                    // add original tag without modification
                    addEntry(tag.key, number, tag.key)
                    invalid.add(number)

                    Logging.trace(e)
                    Logging.debug("Problematic number found '$number' (${primitive.type}${primitive.id})")
                    continue
                }
            }
        }
    }

    /**
     * Add a new number to the given tag. Also eliminates duplicated numbers in the given key.
     * @param key object key
     * @param value new value
     * @param oldKey the old key used to detect schema and class phone type change
     */
    private fun addEntry(key: String, value: String, oldKey: String) {
        processedNumbers.getOrPut(prefix + key.substringAfter(CONTACT_SCHEME_PREFIX)) { linkedSetOf() }.add(value)

        // detect class change
        if (oldKey.substringAfter(CONTACT_SCHEME_PREFIX) != key.substringAfter(CONTACT_SCHEME_PREFIX))
            hasSwitchedClass = true

        // detect schema change
        if (!oldKey.contains(CONTACT_SCHEME_PREFIX) && prefix == CONTACT_SCHEME_PREFIX)
            hasSchemaChange = true
    }

    /**
     * Replies processed numbers as TagMap.
     */
    fun getTagMap(): TagMap {
        return TagMap(getAsMap())
    }

    /**
     * Replies processed numbers as a Java map. Also, keys with empty string queued for removal.
     */
    fun getAsMap(): Map<String, String> {
        val map = HashMap<String, String>()

        for (entry in processedNumbers)
            map[entry.key] = entry.value.joinToString(SEP.toString())

        //add keys with empty values for auto removal
        for (key in originalNumbers.keys - map.keys)
            map[key] = ""

        return map
    }

    /**
     * Returns if the given OSM object had at least one premium calling rate number.
     */
    private fun hasPremiumNumber(): Boolean {
        return premiumNumber.isNotEmpty()
    }

    /**
     * Returns if the given OSM object had duplicated phone numbers,
     * excluding cases where the phone and fax number matches.
     */
    private fun hasDuplicates(): Boolean {
        val orig = originalNumbers.values.sumOf { arrayList: ArrayList<String> -> arrayList.size }
        val proc = processedNumbers.values.sumOf { linkedHashSet: LinkedHashSet<String> -> linkedHashSet.size }

        return orig - (proc + premiumNumber.size + unusualChars.size + tooShort.size + invalid.size
            + notWellFormatted.size) > 0
    }

    /**
     * Returns true if any of the phone numbers has separator issue.
     */
    private fun hasWrongSeparator(): Boolean {
        return badSeparator.isNotEmpty()
    }

    /**
     * Returns true if the primitive is fixable.
     */
    fun isFixable(): Boolean {
        return hasDuplicates() || hasPremiumNumber() || hasWrongSeparator() || isFormatted || isBeautifyable || hasSwitchedClass || hasSchemaChange
    }

    /**
     * Returns the validator description of auto-fixable issues.
     *
     * @return an array of auto-fixable issue messages
     */
    fun getValidatorDescription(): ArrayList<String> {
        val description = arrayListOf<String>()

        if (hasDuplicates())
            description.add(tr("duplicate values"))
        if (hasPremiumNumber())
            description.add(
                trn("premium rate number", "premium rate numbers", premiumNumber.size.toLong())
            )
        if (isFormatted)
            description.add(tr("not in E.123 format"))
        else if (isBeautifyable)
            description.add(tr("beautifiable value"))
        if (hasSwitchedClass)
            description.add(tr("inappropriate key"))
        if (hasSchemaChange)
            description.add(tr("contact: prefix scheme recommended"))

        return description
    }
}
