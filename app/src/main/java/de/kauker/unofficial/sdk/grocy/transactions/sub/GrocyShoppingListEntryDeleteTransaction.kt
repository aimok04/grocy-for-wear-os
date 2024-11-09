package de.kauker.unofficial.sdk.grocy.transactions.sub

import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry
import kotlinx.serialization.Serializable

@Serializable
class GrocyShoppingListEntryDeleteTransaction(
    private val entryId: String
) : GrocyTransaction() {

    override fun apply(grocyClient: GrocyClient) {
        grocyClient.OBJECTS_SHOPPING_LIST_ENTRIES.remove(entryId)
        grocyClient.shoppingListEntries.removeIf { it.id == entryId }
        super.apply(grocyClient)
    }

    override fun flush(grocyClient: GrocyClient): Boolean {
        try {
            /* accept even when request is not successful */
            return GrocyRequest(grocyClient).delete(
                "/api/objects/shopping_list/${entryId}",
                "{}"
            ).code.toString().let { it.startsWith("2") || it.startsWith("4") || it.startsWith("5") }
        } catch(throwable: Throwable) {
            throwable.printStackTrace()
        }

        return false
    }

}

suspend fun GrocyShoppingListEntry.delete(): GrocyShoppingListEntryDeleteTransaction? {
    return this.grocyClient!!.transactionsManager.addTransaction(
        GrocyShoppingListEntryDeleteTransaction(
            this.id
        )
    )
}
