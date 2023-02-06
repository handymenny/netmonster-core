package cz.mroczis.netmonster.sample.storage

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant


class CellAdapter : JsonSerializer<Cell?> {
    override fun serialize(
        src: Cell?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObj = JsonObject()
        src?.let { cell ->
            jsonObj.addProperty("area", cell.area)
            jsonObj.addProperty("caught", Instant.ofEpochMilli(cell.caught).toString())
            jsonObj.addProperty("cid", cell.cid)
            jsonObj.addProperty("code", cell.code)
            jsonObj.addProperty("frequency", cell.frequency)
            jsonObj.addProperty("latitude", cell.latitude)
            jsonObj.addProperty("location", cell.location)
            jsonObj.addProperty("longitude", cell.longitude)
            val networkObject = JsonObject()
            networkObject.addProperty("mcc", cell.network.split("-").first())
            networkObject.addProperty("mnc", cell.network.split("-").last())
            jsonObj.add("network", networkObject)
            jsonObj.addProperty("technology", cell.technology)
        }

        return jsonObj
    }
}
