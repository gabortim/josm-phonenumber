package com.github.gabortim.phonenumber.test

import com.github.gabortim.phonenumber.tool.PrimitiveGeocoder.getIso3166Alpha2Code
import org.openstreetmap.josm.actions.ExpertToggleAction
import org.openstreetmap.josm.actions.ExpertToggleAction.ExpertModeChangeListener
import org.openstreetmap.josm.command.ChangePropertyCommand
import org.openstreetmap.josm.command.Command
import org.openstreetmap.josm.command.SequenceCommand
import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.data.preferences.BooleanProperty
import org.openstreetmap.josm.data.preferences.sources.ValidatorPrefHelper
import org.openstreetmap.josm.data.validation.Severity
import org.openstreetmap.josm.data.validation.Test.TagTest
import org.openstreetmap.josm.data.validation.TestError
import org.openstreetmap.josm.tools.Destroyable
import org.openstreetmap.josm.tools.GBC
import org.openstreetmap.josm.tools.I18n.tr
import org.openstreetmap.josm.tools.PatternUtils
import javax.swing.JCheckBox
import javax.swing.JPanel


// Error codes
const val PARSE_ERROR = 10600
const val INVALID_NUMBER = 10601
const val WRONG_REGION = 10602
const val BAD_FORMAT = 10603
const val BAD_SEPARATOR = 10604
const val TOO_SHORT_NUMBER = 10605
const val TOO_FEW_GROUPING = 10606

/** Value for batch error warnings and fixes */
const val MULTI = 10607

/**
 * Semicolon as the tag value separator.
 */
const val SEP = ';'


/**
 * Main class of the phone number validator plugin.
 *
 * Uses JOSM validator codes from 10600 to 10610.
 */
class PhoneNumberValidator : TagTest(
    tr("Phone number"), tr("Checks all common phone keys and values")
), ExpertModeChangeListener, Destroyable {
    companion object {
        val usableKeys = setOf(
            "phone",            // 2.026.095 instances (taginfo as of 2022-01-25)
            "contact:phone",    // 544.454
            "fax",              // 157.442
            "contact:fax",      // 73.495
            "contact:mobile",   // 21.596
            "phone:mobile"      // 4.166
        )

        /**
         * @return true if the key contains usable key for the plugin. Includes number suffixed keys, like phone_1.
         */
        fun isKeyUsable(key: String): Boolean {
            return usableKeys.any { s: String -> PatternUtils.compile("${s}(_\\d*)?").matcher(key).matches() }
        }
    }

    /**
     * JOSM registry setting prefix for the plugin preferences.
     */
    private val prefix = ValidatorPrefHelper.PREFIX + "." + PhoneNumberValidator::class.java.simpleName

    private val autofixProperty = BooleanProperty("$prefix.autofix", false)
    private val forceContactSchemeProperty = BooleanProperty("$prefix.force_contactScheme", true)

    private val checkboxAutofix = JCheckBox(tr("EXPERIMENTAL! Enable autofix"))

    private lateinit var parsedNumbers: PhoneNumber

    override fun addGui(testPanel: JPanel) {
        super.addGui(testPanel)

        // only enable the selectable auto fix when expert mode used
        if (ExpertToggleAction.isExpert()) {
            checkboxAutofix.isSelected = autofixProperty.get()
            testPanel.add(checkboxAutofix, GBC.eol().insets(20, 0, 0, 0))
        }
    }

    override fun ok(): Boolean {
        super.ok()
        autofixProperty.put(checkboxAutofix.isSelected)
        return false
    }

    override fun expertChanged(isExpert: Boolean) {
        if (!isExpert)  // disable the autofix, when expert mode disabled
            autofixProperty.put(false)
    }

    override fun isPrimitiveUsable(p: OsmPrimitive): Boolean {
        return super.isPrimitiveUsable(p) && p.bBox.isValid && hasUsableKey(p)
    }

    override fun check(primitive: OsmPrimitive) {
        parsedNumbers = PhoneNumber(primitive, getIso3166Alpha2Code(primitive), forceContactSchemeProperty.get())

        if (parsedNumbers.processedNumbers.isEmpty()) return

        for (key in parsedNumbers.badSeparator) {
            errors.add(
                TestError.builder(this, Severity.ERROR, BAD_SEPARATOR)
                    .message(
                        tr("Phone number invalid"),
                        tr("wrong separator used in {0} key", key)
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        for (value in parsedNumbers.invalid) {
            errors.add(
                TestError.builder(this, Severity.ERROR, PARSE_ERROR)
                    .message(
                        tr("Phone number invalid"),
                        tr("couldn''t parse {0}", value)
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        for (value in parsedNumbers.tooShort) {
            errors.add(
                TestError.builder(this, Severity.ERROR, TOO_SHORT_NUMBER)
                    .message(
                        tr("Phone number invalid"),
                        tr("too short {0}", value)
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        for (value in parsedNumbers.unusualChars) {
            errors.add(
                TestError.builder(this, Severity.ERROR, INVALID_NUMBER)
                    .message(
                        tr("Phone number invalid"),
                        tr("unusual chars {0}", value)
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        for (value in parsedNumbers.notWellFormatted) {
            errors.add(
                TestError.builder(this, Severity.WARNING, TOO_FEW_GROUPING)
                    .message(
                        tr("Phone number invalid"),
                        tr("Not enough grouping characters. Is it contains extension?"), value
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        if (parsedNumbers.isFixable()) {
            errors.add(
                TestError.builder(this, Severity.WARNING, MULTI)
                    .message(
                        tr("Phone number issues"),
                        parsedNumbers.getValidatorDescription().joinToString()
                    )
                    .primitives(primitive)
                    .build()
            )
        }
        for (region in parsedNumbers.inWrongRegion) {
            errors.add(
                TestError.builder(this, Severity.WARNING, WRONG_REGION)
                    .message(
                        tr("Phone number possibly in wrong region"),
                        region.ifEmpty { tr("<empty>") }
                    )
                    .primitives(primitive)
                    .build()
            )
        }
    }

    override fun fixError(testError: TestError): Command? {
        val commands: MutableList<Command> = ArrayList()

        for (primitive in testError.primitives) {
            parsedNumbers = PhoneNumber(primitive, getIso3166Alpha2Code(primitive), forceContactSchemeProperty.get())

            commands += ChangePropertyCommand(
                setOf(primitive),
                parsedNumbers.getAsMap()
            )
        }

        return when (commands.size) {
            0 -> null
            1 -> commands[0]
            else -> SequenceCommand(tr("Fix phone number values"), commands)
        }
    }

    override fun isFixable(testError: TestError): Boolean {
        if (autofixProperty.get() && testError.tester is PhoneNumberValidator) {
            val c = testError.code
            return c == BAD_FORMAT || c == BAD_SEPARATOR || c == MULTI
        }
        return false
    }

    override fun destroy() {
        ExpertToggleAction.removeExpertModeChangeListener(this)
        autofixProperty.remove()
    }

    /**
     * Returns true if the primitive has usable keys
     */
    private fun hasUsableKey(primitive: OsmPrimitive): Boolean {
        return primitive.keys().filter(Companion::isKeyUsable).findAny().isPresent
    }
}