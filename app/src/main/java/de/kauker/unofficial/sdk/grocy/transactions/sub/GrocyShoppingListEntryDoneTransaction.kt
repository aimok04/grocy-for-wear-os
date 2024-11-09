package de.kauker.unofficial.sdk.grocy.transactions.sub

import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
class GrocyShoppingListEntryDoneTransaction(
    private val entryId: String,
    private val done: Boolean
) : GrocyTransaction() {

    override fun apply(grocyClient: GrocyClient) {
        val entry = grocyClient.OBJECTS_SHOPPING_LIST_ENTRIES[entryId]
        entry?.done = done
        super.apply(grocyClient)
    }

    override fun flush(grocyClient: GrocyClient): Boolean {
        try {
            /* accept even when request is not successful */
            return GrocyRequest(grocyClient).put(
                "/api/objects/shopping_list/${entryId}",
                JSONObject().put("done", if(done) "1" else "0").toString()
            ).code.toString().let { it.startsWith("2") || it.startsWith("4") || it.startsWith("5") }
        } catch(throwable: Throwable) {
            throwable.printStackTrace()
        }

        return false
    }

}

suspend fun GrocyShoppingListEntry.done(done: Boolean): GrocyShoppingListEntryDoneTransaction? {
    return this.grocyClient!!.transactionsManager.addTransaction(
        GrocyShoppingListEntryDoneTransaction(
            this.id,
            done
        )
    )
}
