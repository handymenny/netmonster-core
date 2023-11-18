package cz.mroczis.netmonster.core.feature.postprocess

import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.core.util.isSamsung
import kotlin.math.log10

/**
 * Some Samsung devices tend to return an RSSI = RSRP.
 * This postprocessor will use an estimated RSSI if an invalid RSSI is detected
 */
class RssiPostprocessor : ICellPostprocessor {

    override val id = CellPostprocessor.RSSI_POSTPROCESSOR

    override fun postprocess(list: List<ICell>): List<ICell> {
        return if (isSamsung()) {
            list.map { cell ->
                // Only correct Primary LTE Cells
                if (cell is CellLte && cell.connectionStatus is PrimaryConnection && cell.shouldRecalculateRSSI()) {
                    cell.withRecalculatedRSSI()
                } else cell
            }
        } else list
    }

    private fun CellLte.shouldRecalculateRSSI(): Boolean {
        // rsrq, rsrp and rssi needed to correct invalid rssi
        // 10 is ~ the result of withRecalculatedRSSI with 6 RBs (min) and -3 rsrq (max)
        return signal.rssi != null && signal.rsrp != null && signal.rsrq != null && (signal.rssi < signal.rsrp + 10)
    }

    /**
     * Recalculate RSSI using formula:
     * RSSI = 10 * log10(RB) + RSRP - RSRQ
     */
    private fun CellLte.withRecalculatedRSSI(): CellLte {
        val rsrp = signal.rsrp!!
        val rsrq = signal.rsrq!!
        val rb = bandwidth?.times(0.005) ?: 75

        val newRssi = 10 * log10(rb.toDouble()) + rsrp - rsrq

        return copy(signal = signal.copy(rssi = newRssi.toInt()))
    }
}