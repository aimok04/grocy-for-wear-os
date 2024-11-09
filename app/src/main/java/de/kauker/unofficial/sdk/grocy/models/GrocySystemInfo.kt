package de.kauker.unofficial.sdk.grocy.models

import de.kauker.unofficial.grocy.utils.JsonAsStringSerializer
import de.kauker.unofficial.grocy.utils.jsonInstance
import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.GrocyRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GrocySystemInfo(
    @SerialName("grocy_version")
    val grocyVersion: GrocySystemInfoVersion,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("php_version")
    val phpVersion: String,

    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("sqlite_version")
    val sqlliteVersion: String,

    @Serializable(with = JsonAsStringSerializer::class)
    val os: String,

    @Serializable(with = JsonAsStringSerializer::class)
    val client: String
) {
    override fun toString(): String {
        return "GrocySystemInfo(grocyVersion=$grocyVersion, phpVersion='$phpVersion', sqlliteVersion='$sqlliteVersion', os='$os', client='$client')"
    }
}

@Serializable
class GrocySystemInfoVersion(
    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("Version")
    val version: String,
    @Serializable(with = JsonAsStringSerializer::class)
    @SerialName("ReleaseDate")
    val releaseDate: String
) {
    override fun toString(): String {
        return "GrocySystemInfoVersion(version='$version', releaseDate='$releaseDate')"
    }
}

fun GrocyClient.retrieveSystemInfo(): GrocySystemInfo {
    val response = GrocyRequest(this).get("/api/system/info", false)
    return jsonInstance.decodeFromString<GrocySystemInfo>(response ?: "")
}
