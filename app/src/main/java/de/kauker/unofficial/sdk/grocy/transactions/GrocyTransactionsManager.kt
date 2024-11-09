package de.kauker.unofficial.sdk.grocy.transactions

import android.content.Context
import de.kauker.unofficial.DURATION_MAX_TRANSACTION_AGE
import de.kauker.unofficial.DURATION_MIN_TRANSACTION_AGE_FOR_SYNC
import de.kauker.unofficial.DURATION_TRANSACTION_SYNC_TIMEOUT
import de.kauker.unofficial.LIMIT_MAX_TRANSACTION_COUNT
import de.kauker.unofficial.grocy.utils.jsonInstance
import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.transactions.sub.GrocyTransaction
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.internal.filterList

class GrocyTransactionsManager(
    context: Context,
    val grocyClient: GrocyClient
) {

    private val transactionList = mutableListOf<GrocyTransaction>()
    private val transactionStorage =
        context.getSharedPreferences("transactions", Context.MODE_PRIVATE)

    private fun loadTransactions() {
        val maxAge = System.currentTimeMillis() - DURATION_MAX_TRANSACTION_AGE

        val jsonArray = jsonInstance.decodeFromString<JsonArray>(
            transactionStorage.getString(
                "savedTransactions",
                "[]"
            ) ?: "[]"
        )
        jsonArray.forEach {
            val transaction = jsonInstance.decodeFromJsonElement<GrocyTransaction>(it)

            /* don't add transaction if older than DURATION_MAX_TRANSACTION_AGE */
            if(transaction.unixMs < maxAge) return@forEach

            transactionList.add(transaction)
        }
    }

    private fun saveTransactions() {
        val jsonElement = jsonInstance.encodeToJsonElement(transactionList)

        transactionStorage.edit().putString("savedTransactions", jsonElement.toString())
            .apply()
    }

    init {
        loadTransactions()
    }

    suspend fun <T : GrocyTransaction> addTransaction(transaction: T): T? {
        /* do not allow more than LIMIT_MAX_TRANSACTION_COUNT transactions */
        if(transactionList.count { !it.completed } > LIMIT_MAX_TRANSACTION_COUNT) return null

        transaction.apply(grocyClient)
        transaction.unixMs = System.currentTimeMillis()

        transactionList.add(transaction)
        saveTransactions()

        CoroutineScope(coroutineContext).launch {
            /* return if flash is unsuccessful */
            if(!transaction.flush(grocyClient)) return@launch

            /* add completed tag when successful */
            transaction.completed = true
            saveTransactions()
        }

        return transaction
    }

    fun applyAll(cached: Boolean) {
        if(!cached) {
            /* remove all completed transactions when data source is not the cache */
            transactionList.removeIf { it.completed }
            saveTransactions()
        }

        transactionList.forEach { it.apply(grocyClient) }
    }

    suspend fun flushAll(state: (failed: Boolean, totalTransactionCount: Int, completedTransactionCount: Int) -> Unit) {
        CoroutineScope(coroutineContext).launch {
            /* remove all completed transactions to mitigate double requests */
            transactionList.removeIf { it.completed }

            /* only sync transactions that are older than DURATION_MIN_TRANSACTION_AGE_FOR_SYNC */
            val minAge = System.currentTimeMillis() - DURATION_MIN_TRANSACTION_AGE_FOR_SYNC
            val syncTransactionList = transactionList.filterList { this.unixMs < minAge }

            val totalTransactionCount = syncTransactionList.size
            var completedTransactionCount = 0

            val startUnixMs = System.currentTimeMillis()

            for(it in syncTransactionList) {
                if(it.flush(grocyClient)) {
                    it.completed = true
                    saveTransactions()
                }

                completedTransactionCount++
                state(false, totalTransactionCount, completedTransactionCount)

                /* stop sync when taking longer than DURATION_TRANSACTION_SYNC_TIMEOUT */
                if((System.currentTimeMillis() - startUnixMs) < DURATION_TRANSACTION_SYNC_TIMEOUT) continue
                state(true, 1, 1)
            }
        }
    }

}
