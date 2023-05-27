package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import org.json.JSONObject

class GrocyProduct(
    val grocyClient: GrocyClient,
    data: JSONObject
) {

    lateinit var id: String
    lateinit var name: String
    lateinit var description: String
    lateinit var timestamp: String

    var productGroup: GrocyProductGroup? = null
    lateinit var _productGroupId: String

    var active: Boolean = false

    var location: GrocyLocation? = null
    lateinit var _locationId: String

    var shoppingLocation: GrocyLocation? = null
    lateinit var _shoppingLocationId: String

    var quantityUnitPurchase: GrocyQuantityUnit? = null
    lateinit var _quantityUnitPurchaseId: String

    var quantityUnitStock: GrocyQuantityUnit? = null
    lateinit var _quantityUnitStockId: String

    var quantityTranslateFactor: String? = null

    init {
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getString("id")
        name = json.getString("name")
        description = json.getString("description")
        _productGroupId = json.getString("product_group_id")
        active = json.getString("active").equals("1")
        _locationId = json.getString("location_id")
        _shoppingLocationId = json.getString("shopping_location_id")
        _quantityUnitPurchaseId = json.getString("qu_id_purchase")
        _quantityUnitStockId = json.getString("qu_id_stock")
        quantityTranslateFactor = json.getString("qu_factor_purchase_to_stock")
        timestamp = json.getString("row_created_timestamp")
    }

    override fun toString(): String {
        return "GrocyProduct(id='$id', name='$name', description='$description', timestamp='$timestamp', productGroup=$productGroup, _productGroupId='$_productGroupId', active=$active, location=$location, _locationId='$_locationId', shoppingLocation=$shoppingLocation, _shoppingLocationId='$_shoppingLocationId', quantityUnitPurchase=$quantityUnitPurchase, _quantityUnitPurchaseId='$_quantityUnitPurchaseId', quantityUnitStock=$quantityUnitStock, _quantityUnitStockId='$_quantityUnitStockId', quantityTranslateFactor='$quantityTranslateFactor')"
    }

    /* api calls */
    fun addToShoppingList(shoppingListId: Int, amount: Int): Boolean {
        val post = JSONObject()
        post.put("product_id", this.id)
        post.put("amount", amount)
        post.put("shopping_list_id", shoppingListId)

        try {
            return GrocyRequest(grocyClient).post(
                "/api/objects/shopping_list",
                post.toString()
            ).isSuccessful
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        return false
    }

}
