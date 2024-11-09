package de.kauker.unofficial.sdk.grocy

import java.util.Date
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class GrocyRequest(private val grocyClient: GrocyClient) {

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

    fun delete(endpoint: String, body: String): Response {
        val request = Request.Builder()
            .url(grocyClient.serverUrl + endpoint)
            .addHeader("GROCY-API-KEY", grocyClient.apiToken)
            .addHeader("content-type", "application/json")
            .delete(body.toRequestBody())
            .build()

        return grocyClient.okHttpClient.newCall(request).execute()
    }

    fun post(endpoint: String, post: String): Response {
        val request = Request.Builder()
            .url(grocyClient.serverUrl + endpoint)
            .addHeader("GROCY-API-KEY", grocyClient.apiToken)
            .addHeader("content-type", "application/json")
            .post(post.toRequestBody())
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
