package de.kauker.unofficial.sdk.grocy

import java.util.*
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class GrocyRequest(grocyClient: GrocyClient) {

    private val grocyClient: GrocyClient

    init {
        this.grocyClient = grocyClient
    }

    fun get(endpoint: String, cached: Boolean): String? {
        val url = grocyClient.serverUrl + endpoint

        if(cached) {
            val cachedContent = grocyClient.cache.getString(url, null)
            if(cachedContent != null) return cachedContent
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("GROCY-API-KEY", grocyClient.apiToken)
            .get()
            .build()

        val content = grocyClient.okHttpClient.newCall(request).execute().body?.string()
        grocyClient.cache.edit()
            .putString(url, content)
            .putLong("latestCacheDate", Date().time).apply()

        return content
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
