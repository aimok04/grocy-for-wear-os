package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GrocyQuantityUnit(
    @Serializable(with = JsonAsStringSerializer::class)
    var id: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var name: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("name_plural")
    var namePlural: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    var description: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("row_created_timestmap")
    var timestamp: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("plural_forms")
    var pluralForms: String?
)
