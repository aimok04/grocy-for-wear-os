package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GrocyLocation(
    @Serializable(with = JsonAsStringSerializer::class)
    var id: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var name: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    var description: String?,

    fun parse(json: JSONObject) {
        id = json.getString("id")
        name = json.getString("name")
        description = json.getString("description")
        timestamp = json.getString("row_created_timestamp")
        isFreezer = json.getString("is_freezer").equals("1")
    }

    override fun toString(): String {
        return "GrocyLocation(id='$id', name='$name', description='$description', timestamp='$timestamp', isFreezer=$isFreezer)"
    }
}
