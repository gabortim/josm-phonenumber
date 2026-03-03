package com.github.gabortim.phonenumber.action

import com.github.gabortim.phonenumber.tool.NumberTools
import com.github.gabortim.phonenumber.validation.PhoneNumberValidator.Companion.validatorPrefix
import com.github.gabortim.phonenumber.validation.ValidatorConstants.CONTACT_SCHEME_PREFIX
import com.github.gabortim.phonenumber.validation.ValidatorConstants.SEP
import org.openstreetmap.josm.actions.JosmAction
import org.openstreetmap.josm.command.ChangePropertyCommand
import org.openstreetmap.josm.command.Command
import org.openstreetmap.josm.command.SequenceCommand
import org.openstreetmap.josm.data.UndoRedoHandler
import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.data.preferences.ListProperty
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.gui.layer.MainLayerManager
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener
import org.openstreetmap.josm.tools.I18n.tr
import java.awt.event.ActionEvent
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class ContactSchemeSwitchAction :
    JosmAction(tr("Switch to contact prefix scheme"), null, null, null, false),
    ActiveLayerChangeListener {
    init {
        putValue(NAME, tr("Switch to contact prefix scheme"))
        putValue(SHORT_DESCRIPTION, tr("Switch this object's contact tags to the newer prefix scheme"))

        MainApplication.getLayerManager().addActiveLayerChangeListener(this)
    }

    companion object {
        private val usableKeys = ListProperty(
            "$validatorPrefix.panelSwitchKeys",
            mutableListOf("email", "phone", "mobile", "website", "facebook", "fax", "instagram", "youtube", "twitter", "linkedin")
        )
    }

    override fun updateEnabledState(selection: Collection<OsmPrimitive>) {
        isEnabled = selection.any { primitive ->
            primitive.hasKey(*usableKeys.get().toTypedArray())
        }
    }

    override fun actionPerformed(actionEvent: ActionEvent) {
        val primitives = MainApplication.getLayerManager().editDataSet.selected
        val propChangeCmds = mutableListOf<Command>()

        for (primitive in primitives) {
            val mergedProperties = mutableMapOf<String, String>()
            for (key in usableKeys.get()) {
                if (primitive.hasKey(key)) {
                    mergedProperties.putAll(merge(primitive, key))
                }
            }

            if (mergedProperties.isNotEmpty()) {
                propChangeCmds.add(ChangePropertyCommand(listOf(primitive), mergedProperties))
            }
        }

        if (propChangeCmds.isNotEmpty()) {
            val seqCmds = SequenceCommand(tr("Switch to contact prefix scheme"), propChangeCmds)
            UndoRedoHandler.getInstance().add(seqCmds, true)
        }
    }

    /**
     * Merges the given primitive's key to the new contact prefix scheme.
     *
     * @param primitive OSM primitive for which change the tags
     * @param key tag key to move the values to the prefix scheme
     */
    private fun merge(primitive: OsmPrimitive, key: String): Map<String, String> {
        val oldValue = primitive.get(key) ?: ""
        val prefixedValue = primitive.get(CONTACT_SCHEME_PREFIX + key) ?: ""

        val newValue = listOf(oldValue, prefixedValue)
            .flatMap { NumberTools.splitAndStrip(it) }
            .distinct()
            .joinToString(SEP.toString())

        return mapOf(
            key to "",                                  // remove old key by mapping it to an empty string
            CONTACT_SCHEME_PREFIX + key to newValue     // add new key
        )
    }

    override fun activeOrEditLayerChanged(l: MainLayerManager.ActiveLayerChangeEvent?) {
        if (l?.previousActiveLayer == null && MainApplication.isDisplayingMapView()) {
            val popupMenuHandler = MainApplication.getMap().propertiesDialog.propertyPopupMenuHandler
            //TODO: after setupTagsMenu() call invalidate() needed
            // also the action is not added to the end of the list

            popupMenuHandler.addSeparator()
            popupMenuHandler.addAction(this)
            popupMenuHandler.addListener(object : PopupMenuListener {
                override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                    updateEnabledStateOnCurrentSelection()
                }

                override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent) {
                    // Do nothing
                }

                override fun popupMenuCanceled(e: PopupMenuEvent) {
                    // Do nothing
                }
            })
        }
    }

    override fun destroy() {
        MainApplication.getLayerManager().removeActiveLayerChangeListener(this)
    }
}