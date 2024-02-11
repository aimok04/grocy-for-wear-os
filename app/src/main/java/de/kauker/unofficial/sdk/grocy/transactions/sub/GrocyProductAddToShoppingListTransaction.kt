package de.kauker.unofficial.sdk.grocy.transactions.sub

import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import de.kauker.unofficial.sdk.grocy.models.GrocyProduct
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingList
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry
import java.util.Date
import java.util.UUID
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
class GrocyProductAddToShoppingListTransaction(
    private val productId: String,
    private val listId: String,
    private val amount: Int
): GrocyTransaction() {

    private val simulateId: String = UUID.randomUUID().toString()

    override fun apply(grocyClient: GrocyClient) {
        val product = grocyClient.OBJECTS_PRODUCTS[productId]

        val entry = GrocyShoppingListEntry(
            id = simulateId,
            _productId = productId,
            note = "",
            amount = amount.toString(),
            timestamp = Date().toString(),
            shoppingListId = listId,
            _done = "0",
            _quantityUnitId = grocyClient.OBJECTS_PRODUCTS[productId]?.quantityUnitPurchaseId
        )

        entry.grocyClient = grocyClient
        entry.product = product
        entry.quantityUnit = product?.quantityUnitPurchase

        grocyClient.OBJECTS_SHOPPING_LIST_ENTRIES[simulateId] = entry
        grocyClient.shoppingListEntries.add(entry)

        super.apply(grocyClient)
    }

    override fun flush(grocyClient: GrocyClient): Boolean {
        val post = JSONObject()
        post.put("product_id", productId)
        post.put("amount", amount)
        post.put("shopping_list_id", listId)

        try {
            /* accept even when request is not successful */
            return GrocyRequest(grocyClient).post(
                "/api/objects/shopping_list",
                post.toString()
            ).code.toString().let { it.startsWith("2") || it.startsWith("4") || it.startsWith("5") }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        return false
    }

}

suspend fun GrocyProduct.addToShoppingList(shoppingList: GrocyShoppingList, amount: Int): GrocyProductAddToShoppingListTransaction? {
    return this.grocyClient!!.transactionsManager.addTransaction(
        GrocyProductAddToShoppingListTransaction(
            this.id,
            shoppingList.id,
            amount
        )
    )
}
