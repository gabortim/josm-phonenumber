package com.github.gabortim.phonenumber.action

import com.github.gabortim.phonenumber.test.CONTACT_SCHEME_PREFIX
import com.github.gabortim.phonenumber.test.SEP
import com.github.gabortim.phonenumber.tool.NumberTools
import org.openstreetmap.josm.actions.JosmAction
import org.openstreetmap.josm.command.ChangePropertyCommand
import org.openstreetmap.josm.command.Command
import org.openstreetmap.josm.command.SequenceCommand
import org.openstreetmap.josm.data.UndoRedoHandler
import org.openstreetmap.josm.data.osm.OsmPrimitive
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.gui.layer.MainLayerManager
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener
import org.openstreetmap.josm.tools.I18n.tr
import java.awt.event.ActionEvent

class ContactSchemeSwitchAction :
    JosmAction(tr("Switch to contact prefix scheme"), null, null, null, false),
    ActiveLayerChangeListener {
    init {
        putValue(NAME, tr("Switch to contact prefix scheme"))
        putValue(SHORT_DESCRIPTION, tr("Switch this object's contact tags to the newer prefix scheme"))

        MainApplication.getLayerManager().addActiveLayerChangeListener(this)
    }

    companion object {
        private val usableKeys = arrayOf("email", "phone", "mobile", "website", "facebook", "fax")
    }

    override fun updateEnabledState(selection: Collection<OsmPrimitive>) {
        for (primitive in selection) {
            if (primitive.hasKey(*usableKeys)) {
                isEnabled = true
                return
            }
        }
        isEnabled = false
    }

    override fun actionPerformed(actionEvent: ActionEvent) {
        val primitives = MainApplication.getLayerManager().editDataSet.selected

        val propChangeCmds = ArrayList<ChangePropertyCommand>()

        for (primitive in primitives) {
            for (key in usableKeys) {
                if (primitive.hasKey(key)) {
                    // use new key with prefix
                    val result = merge(primitive, key)

                    // should not happen, ever
                    assert(result.isNotEmpty()) { "should not happen, ever" }

                    propChangeCmds.add(ChangePropertyCommand(listOf(primitive), result))
                }
            }
        }

        val seqCmds = SequenceCommand(tr("Switch to contact prefix scheme"), propChangeCmds as Collection<Command>)

        UndoRedoHandler.getInstance().add(seqCmds, true)

        updateEnabledState(primitives)
    }

    /**
     * Merges the given primitive's key to the new contact prefix scheme.
     *
     * @param primitive OSM primitive for which change the tags
     * @param key tag key to move the values to the prefix scheme
     */
    private fun merge(primitive: OsmPrimitive, key: String): Map<String, String> {
        if (!primitive.hasKey(key))
            return mapOf()

        val oldValue = primitive.get(key) ?: ""

        // if the prefixed tag is nonexistent, the string will end with semicolon, it's not an issue
        var newValue = oldValue + SEP + (primitive.get(CONTACT_SCHEME_PREFIX + key) ?: "")

        newValue = LinkedHashSet(NumberTools.splitAndStrip(newValue)).joinToString(SEP.toString())

        return mapOf(
            key to "",                                  // remove old key by mapping it to an empty string
            CONTACT_SCHEME_PREFIX + key to newValue     // add new key
        )
    }

    override fun activeOrEditLayerChanged(e: MainLayerManager.ActiveLayerChangeEvent?) {
        if (e?.previousActiveLayer == null) {
            if (MainApplication.isDisplayingMapView()) {
                val propertiesDialog = MainApplication.getMap().propertiesDialog
                val popupMenuHandler = propertiesDialog.propertyPopupMenuHandler
                //TODO: after setupTagsMenu() call invalidate() needed
                // also the action is not added to the end of the list

                popupMenuHandler.addSeparator()
                popupMenuHandler.addAction(this)
            }
        }
    }
}