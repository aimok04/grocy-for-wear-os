package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
class GrocyShoppingList(
    @Serializable(with = JsonAsStringSerializer::class)
    var id: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var name: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    var description: String?
)
