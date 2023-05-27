package de.kauker.unofficial.sdk.grocy.models

import org.json.JSONObject

class GrocyShoppingList(
    data: JSONObject
) {

    lateinit var id: Number
    lateinit var name: String
    lateinit var description: String

    init {
        parse(data)
    }

    fun parse(json: JSONObject) {
        id = json.getInt("id")
        name = json.getString("name")
        description = json.getString("description")
    }
}
