package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import de.kauker.unofficial.sdk.grocy.GrocyClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class GrocyShoppingListEntry(
    @Serializable(with = JsonAsStringSerializer::class)
    var id: String,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("product_id")
    var _productId: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var note: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    var amount: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("row_created_timestamp")
    var timestamp: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("shopping_list_id")
    var shoppingListId: String,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("done")
    var _done: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("qu_id")
    var _quantityUnitId: String?
) {

    @Transient
    var grocyClient: GrocyClient? = null

    @Transient
    var product: GrocyProduct? = null
    @Transient
    var quantityUnit: GrocyQuantityUnit? = null

    var done: Boolean
        get() {
            return _done == "1"
        }
        set(value) {
            this._done = if(value) "1" else "0"
        }

}
