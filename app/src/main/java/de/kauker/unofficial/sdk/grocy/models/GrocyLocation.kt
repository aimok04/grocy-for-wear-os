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

    @SerialName("row_created_timestamp")
    var timestamp: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    var is_freezer: String?
) {
    val isFreezer: Boolean
        get() {
            return is_freezer == "1"
        }

}
