package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import de.kauker.unofficial.sdk.grocy.GrocyClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class GrocyProduct(
    @Serializable(with = JsonAsStringSerializer::class)
    var id: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var name: String,

    @Serializable(with = JsonAsStringSerializer::class)
    var description: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("row_created_timestamp")
    var timestamp: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("product_group_id")
    var productGroupId: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("active")
    var _active: String,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("location_id")
    var locationId: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("shopping_location_id")
    var shoppingLocationId: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("qu_id_purchase")
    var quantityUnitPurchaseId: String?,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("qu_id_stock")
    var quantityUnitStockId: String?
) {

    @Transient
    var grocyClient: GrocyClient? = null

    var active: Boolean
        get() {
            return _active == "1"
        }
        set(value) {
            _active = if(value) "1" else "0"
        }

    @Transient
    var productGroup: GrocyProductGroup? = null

    @Transient
    var location: GrocyLocation? = null
    @Transient
    var shoppingLocation: GrocyLocation? = null
    @Transient
    var quantityUnitPurchase: GrocyQuantityUnit? = null
    @Transient
    var quantityUnitStock: GrocyQuantityUnit? = null

}
