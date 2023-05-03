package com.github.gabortim.phonenumber.action

import com.github.gabortim.phonenumber.test.PhoneNumberValidator
import org.openstreetmap.josm.data.Bounds
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.gui.PleaseWaitRunnable
import org.openstreetmap.josm.gui.layer.OsmDataLayer
import org.openstreetmap.josm.io.OsmTransferException
import org.openstreetmap.josm.io.OverpassDownloadReader
import org.openstreetmap.josm.tools.ExceptionUtil
import org.openstreetmap.josm.tools.I18n.tr
import org.openstreetmap.josm.tools.SearchCompilerQueryWizard
import java.net.SocketTimeoutException

class PhoneNumberDownloadTask(private val bounds: Bounds) :
    PleaseWaitRunnable(tr("Download objects via Overpass API")) {
    private var canceled = false
    private var query: String = SearchCompilerQueryWizard.constructQuery(
        PhoneNumberValidator.usableKeys.joinToString(separator = "=* or ") { "\"$it\""}.plus("=*")
    )
    private lateinit var tmpLayer: OsmDataLayer

    override fun cancel() {
        canceled = true
    }

    override fun realRun() {
        try {
            val reader = OverpassDownloadReader(
                bounds, OverpassDownloadReader.OVERPASS_SERVER.get(), query
            )
            val tmpDs = reader.parseOsm(progressMonitor.createSubTaskMonitor(1, false))
            if (!canceled) {
                tmpLayer = OsmDataLayer(tmpDs, OsmDataLayer.createNewName(), null)
            }
        } catch (e: OsmTransferException) {
            if (canceled) return
            ExceptionUtil.explainOsmTransferException(e)
        } catch (e: SocketTimeoutException) {
            if (canceled) return
            ExceptionUtil.explainException(e).replace("<html>", "").replace("</html>", "")
        }
    }

    override fun finish() {
        synchronized(this) {
            if (canceled) return
        }

        // Append downloaded data to JOSM
        val layer = MainApplication.getLayerManager().editLayer
        if (layer == null || !layer.isDownloadable) {
            MainApplication.getLayerManager().addLayer(tmpLayer)
        } else {
            layer.mergeFrom(tmpLayer)
        }
    }
}