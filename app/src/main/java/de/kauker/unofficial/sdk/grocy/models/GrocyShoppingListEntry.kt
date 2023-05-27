package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import org.json.JSONObject

class GrocyShoppingListEntry(
    grocyClient: GrocyClient,
    data: JSONObject
) {

    val grocyClient: GrocyClient

    lateinit var id: String

    var product: GrocyProduct? = null
    lateinit var _productId: String

    lateinit var note: String
    lateinit var amount: String
    lateinit var timestamp: String
    lateinit var shoppingListId: Number
    var done: Boolean = false

    var quantityUnit: GrocyQuantityUnit? = null
    lateinit var _quantityUnitId: String

    init {
        this.grocyClient = grocyClient
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getString("id")
        _productId = json.getString("product_id")
        note = json.getString("note")
        amount = json.getString("amount")
        timestamp = json.getString("row_created_timestamp")
        shoppingListId = json.getInt("shopping_list_id")
        done = json.getString("done").equals("1")
        _quantityUnitId = json.getString("qu_id")
    }

    /* api calls */
    fun delete(): Boolean {
        try {
            return GrocyRequest(grocyClient).delete(
                "/api/objects/shopping_list/${id}",
                "{}"
            ).isSuccessful
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        return false
    }

    private fun edit(post: JSONObject): Boolean {
        try {
            return GrocyRequest(grocyClient).put(
                "/api/objects/shopping_list/${id}",
                post.toString()
            ).isSuccessful
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        return false
    }

    fun setDone(done: Boolean): Boolean {
        return edit(JSONObject().put("done", if (done) "1" else "0"))
    }

}
