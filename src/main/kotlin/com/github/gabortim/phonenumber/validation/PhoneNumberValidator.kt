package com.github.gabortim.phonenumber.validation

import com.github.gabortim.phonenumber.tool.PrimitiveGeocoder.getIso3166Alpha2Code
import org.openstreetmap.josm.actions.ExpertToggleAction
import org.openstreetmap.josm.actions.ExpertToggleAction.ExpertModeChangeListener
import org.openstreetmap.josm.command.ChangePropertyCommand
import org.openstreetmap.josm.command.Command
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
import com.github.gabortim.phonenumber.validation.ValidatorConstants.BAD_SEPARATOR
import com.github.gabortim.phonenumber.validation.ValidatorConstants.INVALID_NUMBER
import com.github.gabortim.phonenumber.validation.ValidatorConstants.MULTI
import com.github.gabortim.phonenumber.validation.ValidatorConstants.PARSE_ERROR
import com.github.gabortim.phonenumber.validation.ValidatorConstants.TOO_FEW_GROUPING
import com.github.gabortim.phonenumber.validation.ValidatorConstants.TOO_SHORT_NUMBER
import com.github.gabortim.phonenumber.validation.ValidatorConstants.WRONG_REGION
import javax.swing.JCheckBox
import javax.swing.JPanel


object ValidatorConstants {
    // Error codes
    const val PARSE_ERROR = 10600
    const val INVALID_NUMBER = 10601
    const val WRONG_REGION = 10602
    // unused code 10603
    const val BAD_SEPARATOR = 10604
    const val TOO_SHORT_NUMBER = 10605
    const val TOO_FEW_GROUPING = 10606

    /** Value for batch error warnings and fixes */
    const val MULTI = 10607

    /** Semicolon as the tag value separator. */
    const val SEP = ';'

    /** Key prefix used for contact tagging scheme */
    const val CONTACT_SCHEME_PREFIX = "contact:"
}


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
            "phone",            // 3.372.499 instances (taginfo as of 2026-01-02)
            "contact:phone",    // 977.972
            "fax",              // 190.245
            "contact:fax",      // 90.630
            "contact:mobile",   // 54.784
            "phone:mobile"      // 5.977
        )

        /**
         * @return true if the key contains usable key for the plugin. Includes number suffixed keys, like phone_1.
         */
        fun isKeyUsable(key: String): Boolean {
            return usableKeys.any { s: String -> PatternUtils.compile("${s}(_\\d*)?").matcher(key).matches() }
        }

        /** JOSM registry setting prefix for the phonenumber plugin preferences. */
        val validatorPrefix = ValidatorPrefHelper.PREFIX + "." + PhoneNumberValidator::class.java.simpleName
    }

    private val autofixProperty = BooleanProperty("$validatorPrefix.autofix", false)
    private val forceContactSchemeProperty = BooleanProperty("$validatorPrefix.forceContactScheme", true)

    private val checkboxAutofix = JCheckBox(tr("BETA! Enable autofix"))
    private val checkboxForceContactScheme = JCheckBox(tr("Enforce contact scheme"))

    override fun addGui(testPanel: JPanel) {
        super.addGui(testPanel)

        // only enable the selectable auto fix when expert mode used
        if (ExpertToggleAction.isExpert()) {
            checkboxAutofix.isSelected = autofixProperty.get()
            testPanel.add(checkboxAutofix, GBC.eol().insets(20, 0, 0, 0))
        }

        checkboxForceContactScheme.isSelected = forceContactSchemeProperty.get()
        testPanel.add(checkboxForceContactScheme, GBC.eol().insets(20, 0, 0, 0))
    }

    override fun ok(): Boolean {
        super.ok()
        autofixProperty.put(checkboxAutofix.isSelected)
        forceContactSchemeProperty.put(checkboxForceContactScheme.isSelected)
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
        val region = getIso3166Alpha2Code(primitive)
        val parsedNumbers = PhoneNumber(primitive, region, forceContactSchemeProperty.get())

        val fixedMap = parsedNumbers.getAsMap()
        val fixCommand = if (fixedMap != primitive.interestingTags) {
            ChangePropertyCommand(listOf(primitive), fixedMap)
        } else null

        val errorBuilder = { severity: Severity, code: Int, message: String, value: String? ->
            val builder = TestError.builder(this, severity, code)
                .message(tr("Phone number invalid"), message, value)
                .primitives(primitive)

            if (fixCommand != null) {
                builder.fix { fixCommand }
            }

            errors.add(builder.build())
        }

        /// non autofixable errors
        for (value in parsedNumbers.invalid) {
            errorBuilder(Severity.ERROR, PARSE_ERROR, tr("couldn''t parse {0}"), value)
        }
        for (value in parsedNumbers.tooShort) {
            errorBuilder(Severity.ERROR, TOO_SHORT_NUMBER, tr("too short {0}"), value)
        }
        for (value in parsedNumbers.unusualChars) {
            errorBuilder(Severity.ERROR, INVALID_NUMBER, tr("unusual chars {0}"), value)
        }
        for (value in parsedNumbers.notWellFormatted) {
            errorBuilder(Severity.WARNING, TOO_FEW_GROUPING, tr("Not enough grouping characters. Is it contains extension?"), value)
        }
        for (regionCode in parsedNumbers.inWrongRegion) {
            val builder = TestError.builder(this, Severity.WARNING, WRONG_REGION)
                .message(tr("Phone number possibly in wrong region"), regionCode.ifEmpty { tr("<empty>") })
                .primitives(primitive)

            if (fixCommand != null)
                builder.fix { fixCommand }

            errors.add(builder.build())
        }

        /// autofixable warnings
        for (key in parsedNumbers.badSeparator) {
            errorBuilder(Severity.ERROR, BAD_SEPARATOR, tr("wrong separator used in {0} key"), key)
        }

        // avoid duplicate warning
        if (parsedNumbers.isFixable() && parsedNumbers.badSeparator.isEmpty()) {
            val builder = TestError.builder(this, Severity.WARNING, MULTI)
                .message(tr("Phone number issues"), parsedNumbers.getValidatorDescription().joinToString())
                .primitives(primitive)

            if (fixCommand != null)
                builder.fix { fixCommand }

            errors.add(builder.build())
        }
    }

    override fun fixError(testError: TestError): Command? {
        return testError.fix
    }

    override fun isFixable(testError: TestError): Boolean {
        if (autofixProperty.get() && testError.tester is PhoneNumberValidator) {
            val c = testError.code
            return c == BAD_SEPARATOR || c == MULTI
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