package de.kauker.unofficial.sdk.grocy.models

import org.json.JSONObject


class GrocyProductGroup(
    data: JSONObject
) {

    lateinit var id: String
    lateinit var name: String
    lateinit var description: String
    lateinit var timestamp: String

    init {
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getString("id")
        name = json.getString("name")
        description = json.getString("description")
        timestamp = json.getString("row_created_timestamp")
    }

    override fun toString(): String {
        return "GrocyProductGroup(id='$id', name='$name', description='$description', timestamp='$timestamp')"
    }
}
