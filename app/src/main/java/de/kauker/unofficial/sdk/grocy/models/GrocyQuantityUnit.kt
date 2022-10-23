package de.kauker.unofficial.sdk.grocy.models

import org.json.JSONObject

class GrocyQuantityUnit(
    data: JSONObject
) {

    lateinit var id: String
    lateinit var name: String
    lateinit var namePlural: String
    lateinit var description: String
    lateinit var timestamp: String
    lateinit var pluralForms: String

    init {
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getString("id")
        name = json.getString("name")
        namePlural = json.getString("name_plural")
        description = json.getString("description")
        timestamp = json.getString("row_created_timestamp")
        pluralForms = json.getString("plural_forms")
    }

    override fun toString(): String {
        return "GrocyQuantityUnit(id='$id', name='$name', namePlural='$namePlural', description='$description', timestamp='$timestamp', pluralForms='$pluralForms')"
    }
}
