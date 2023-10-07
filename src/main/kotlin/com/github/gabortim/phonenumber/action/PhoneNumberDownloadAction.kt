package com.github.gabortim.phonenumber.action

import com.github.gabortim.phonenumber.task.PhoneNumberDownloadTask
import org.openstreetmap.josm.actions.JosmAction
import org.openstreetmap.josm.data.Bounds
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.tools.Destroyable
import org.openstreetmap.josm.tools.I18n.tr
import java.awt.event.ActionEvent


class PhoneNumberDownloadAction : JosmAction(
    tr("Download objects with phone number"),
    "phonenumber_download",
    tr("Download all objects with phone number in the current view"),
    null,
    true
), Destroyable {
    override fun updateEnabledState() {
        val layer = MainApplication.getLayerManager()?.activeLayer

        isEnabled = layer != null
    }

    override fun actionPerformed(event: ActionEvent) {
        val mv = MainApplication.getMap().mapView
        val currentBounds = Bounds(
            mv.getLatLon(0, mv.height),
            mv.getLatLon(mv.width, 0)
        )
        PhoneNumberDownloadTask(currentBounds).run()
    }
}