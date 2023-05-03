package com.github.gabortim.phonenumber

import com.github.gabortim.phonenumber.action.ContactSchemeSwitchAction
import com.github.gabortim.phonenumber.action.PhoneNumberDownloadAction
import com.github.gabortim.phonenumber.test.PhoneNumberValidator
import org.openstreetmap.josm.data.validation.OsmValidator
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.plugins.Plugin
import org.openstreetmap.josm.plugins.PluginInformation
import org.openstreetmap.josm.tools.Destroyable
import javax.swing.Action

/**
 * Will be invoked by JOSM to bootstrap the plugin
 *
 * @param info information about the plugin and its local installation
 */
class PhoneNumberPlugin(info: PluginInformation) : Plugin(info), Destroyable {
    private var phoneNumberDownloadAction: Action
    private var contactSchemeSwitchAction: Action

    init {
        OsmValidator.addTest(PhoneNumberValidator::class.java)

        phoneNumberDownloadAction = PhoneNumberDownloadAction()
        contactSchemeSwitchAction = ContactSchemeSwitchAction()
    }

    override fun destroy() {
        MainApplication.getToolbar().unregister(phoneNumberDownloadAction)
        MainApplication.getToolbar().refreshToolbarControl()
    }
}