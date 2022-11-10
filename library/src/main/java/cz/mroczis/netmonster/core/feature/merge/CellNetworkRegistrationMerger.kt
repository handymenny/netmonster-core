package cz.mroczis.netmonster.core.feature.merge

import cz.mroczis.netmonster.core.model.cell.*
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection

/**
 * Merges cells with cells from network registration info.
 * Merge is simple - if cell with CID is not present in the original list then it's appended
 */
class CellNetworkRegistrationMerger {

    fun merge(existing: List<ICell>, nriCells: List<ICell>): List<ICell> {
        val toAdd = nriCells.filterNot { cell -> cell.hasInvalidCI() || existing containsSimilar cell }
        return existing + toAdd
    }

    private infix fun List<ICell>.containsSimilar(other: ICell) = any { candidate ->
        if (candidate.subscriptionId == other.subscriptionId) {
            val bothPrimaryConnection = candidate.connectionStatus is PrimaryConnection && other.connectionStatus is PrimaryConnection

            if (bothPrimaryConnection) {
                // Happens when data are not synced across the system - multiple primary cells are reported at the same time
                // in such case ignore network registration source
                return true
            }

            when (candidate) {
                is CellGsm -> other is CellGsm && other.cid == candidate.cid
                is CellWcdma -> other is CellWcdma && other.ci == candidate.ci
                is CellLte -> other is CellLte && other.eci == candidate.eci
                is CellNr -> other is CellNr && other.nci == candidate.nci
                is CellCdma -> other is CellCdma && other.bid == candidate.bid
                is CellTdscdma -> other is CellTdscdma && other.ci == candidate.ci
                else -> false
            }
        } else false
    }

    private fun ICell.hasInvalidCI(): Boolean {
        return when (this) {
            is CellGsm -> this.cid == null
            is CellWcdma -> this.ci == null
            is CellLte -> this.eci == null
            is CellNr -> this.nci == null
            is CellCdma -> this.bid == null
            is CellTdscdma -> this.ci == null
            else -> true
        }
    }
}