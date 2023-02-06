package cz.mroczis.netmonster.sample.storage

import java.time.Instant

data class NetmonsterPost(
    val cells: List<Cell>,
) {
    val size = cells.size
    val date = Instant.now().toString()
    val author = "Name that was filled by the sender"
    val contact =  "Optional way to reach sender"
    val comment = "Optional comment by the sender"
    val manufacturer = android.os.Build.MANUFACTURER ?: "Manufacturer"
    val model = android.os.Build.MODEL ?: "Model"
}
