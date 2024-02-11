package de.kauker.unofficial.sdk.grocy.transactions.sub

import de.kauker.unofficial.sdk.grocy.GrocyClient
import kotlinx.serialization.Serializable

@Serializable
sealed class GrocyTransaction {

    var completed = false
    var unixMs = 0L

    /**
     * "Simulates" transaction outcome
     */
    open fun apply(grocyClient: GrocyClient) { }

    /**
     * Sends request to grocy server if possible
     *
     * @return True if successful
     */
    open fun flush(grocyClient: GrocyClient): Boolean {
        return false
    }

}
