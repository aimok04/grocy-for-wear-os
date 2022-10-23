package de.kauker.unofficial.sdk.grocy.models

import org.json.JSONObject

class GrocyLocation(
    data: JSONObject
) {

    lateinit var id: String
    lateinit var name: String
    lateinit var description: String
    lateinit var timestamp: String
    var isFreezer: Boolean = false

    init {
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getString("id")
        name = json.getString("name")
        description = json.getString("description")
        timestamp = json.getString("row_created_timestamp")
        isFreezer = json.getString("is_freezer").equals("1")
    }

    override fun toString(): String {
        return "GrocyLocation(id='$id', name='$name', description='$description', timestamp='$timestamp', isFreezer=$isFreezer)"
    }
}
