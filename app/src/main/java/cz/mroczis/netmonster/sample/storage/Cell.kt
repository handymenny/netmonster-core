package cz.mroczis.netmonster.sample.storage

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.JsonAdapter
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.model.cell.*

@Entity(indices = [Index(value = ["network", "technology", "cid"], unique = true)])
@JsonAdapter(CellAdapter::class)
data class Cell(
    val area: Int?,
    val caught: Milliseconds,
    val cid: Long,
    val code: Int?,
    val frequency: Int?,
    val network: String,
    val technology: String
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
    var location: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    companion object {
        fun valueOf(cell: ICell): Cell? {
            val area: Int?
            val caught = System.currentTimeMillis()
            val cid: Long?
            val code: Int?
            val frequency = cell.band?.channelNumber
            val network = cell.network?.toPlmn("-")
            val technology: String?
            when (cell) {
                is CellLte -> {
                    cid = cell.eci?.toLong()
                    area = cell.tac
                    code = cell.pci
                    technology = Rat.LTE.name
                }
                is CellNr -> {
                    cid = cell.nci
                    area = cell.tac
                    code = cell.pci
                    technology = Rat.NR.name
                }
                is CellWcdma -> {
                    cid = cell.ci?.toLong()
                    area = cell.lac
                    code = cell.psc
                    technology = Rat.WCDMA.name
                }
                is CellGsm -> {
                    cid = cell.cid?.toLong()
                    area = cell.lac
                    code = cell.bsic
                    technology = Rat.GSM.name
                }
                else -> {
                    // We only support 2G, 3G, 4G, 5G
                    return null
                }
            }
            if (cid == null || network == null) {
                // Don't store cells without valid cid or network
                return null
            }
            return Cell(area, caught, cid, code, frequency, network, technology)
        }
    }
}
