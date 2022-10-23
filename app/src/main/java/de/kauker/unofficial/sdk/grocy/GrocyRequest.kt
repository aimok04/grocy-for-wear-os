package de.kauker.unofficial.sdk.grocy

import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class GrocyRequest(grocyClient: GrocyClient) {

    private val grocyClient: GrocyClient

    init {
        this.grocyClient = grocyClient
    }

    fun get(endpoint: String): Response {
        val request = Request.Builder()
            .url(grocyClient.serverUrl + endpoint)
            .addHeader("GROCY-API-KEY", grocyClient.apiToken)
            .get()
            .build()

        return grocyClient.okHttpClient.newCall(request).execute()
    }

    fun put(endpoint: String, put: String): Response {
        val request = Request.Builder()
            .url(grocyClient.serverUrl + endpoint)
            .addHeader("GROCY-API-KEY", grocyClient.apiToken)
            .addHeader("content-type", "application/json")
            .put(put.toRequestBody())
            .build()

        return grocyClient.okHttpClient.newCall(request).execute()
    }

}
