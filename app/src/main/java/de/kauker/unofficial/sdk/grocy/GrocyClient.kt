package de.kauker.unofficial.sdk.grocy

import de.kauker.unofficial.sdk.grocy.models.*
import okhttp3.OkHttpClient
import org.json.JSONArray

class GrocyClient(serverUrl: String, apiToken: String) {

    val OBJECTS_LOCATIONS = HashMap<String, GrocyLocation>()
    val OBJECTS_PRODUCT_GROUPS = HashMap<String, GrocyProductGroup>()
    val OBJECTS_QUANTITY_UNITS = HashMap<String, GrocyQuantityUnit>()

    val OBJECTS_PRODUCTS = HashMap<String, GrocyProduct>()
    val OBJECTS_SHOPPING_LIST_ENTRIES = HashMap<String, GrocyShoppingListEntry>()

    val serverUrl: String
    val apiToken: String

    val okHttpClient: OkHttpClient

    init {
        this.serverUrl = serverUrl
        this.apiToken = apiToken

        this.okHttpClient = OkHttpClient()
    }

    fun fetchShoppingListEntries(): ArrayList<GrocyShoppingListEntry> {
        val list = ArrayList<GrocyShoppingListEntry>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/shopping_list").body!!.string())

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            i++

            if (OBJECTS_SHOPPING_LIST_ENTRIES.containsKey(id)) {
                OBJECTS_SHOPPING_LIST_ENTRIES[id]!!.parse(obj)
                list.add(OBJECTS_SHOPPING_LIST_ENTRIES[id]!!)
                continue
            }

            OBJECTS_SHOPPING_LIST_ENTRIES[id] = GrocyShoppingListEntry(this, obj)
            list.add(OBJECTS_SHOPPING_LIST_ENTRIES[id]!!)
        }

        for (entry in list) {
            if (OBJECTS_PRODUCTS.containsKey(entry._productId)) continue
            fetchProducts()
            break
        }

        for (entry in list) {
            if (OBJECTS_QUANTITY_UNITS.containsKey(entry._quantityUnitId)) continue
            fetchQuantityUnits()
            break
        }

        for (product in list) {
            if (OBJECTS_PRODUCTS.containsKey(product._productId))
                product.product = OBJECTS_PRODUCTS[product._productId]!!
            if (OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitId))
                product.quantityUnit = OBJECTS_QUANTITY_UNITS[product._quantityUnitId]!!
        }

        return list
    }

    private fun fetchProducts(): List<GrocyProduct> {
        val list = ArrayList<GrocyProduct>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/products").body!!.string())

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            i++

            if (OBJECTS_PRODUCTS.containsKey(id)) {
                OBJECTS_PRODUCTS[id]!!.parse(obj)
                list.add(OBJECTS_PRODUCTS[id]!!)
                continue
            }

            OBJECTS_PRODUCTS[id] = GrocyProduct(obj)
            list.add(OBJECTS_PRODUCTS[id]!!)
        }

        for (product in list) {
            if (OBJECTS_LOCATIONS.containsKey(product._locationId)
                && OBJECTS_LOCATIONS.containsKey(product._shoppingLocationId)
            ) continue
            fetchLocations()
            break
        }

        for (product in list) {
            if (OBJECTS_PRODUCT_GROUPS.containsKey(product._productGroupId)) continue
            fetchProductGroups()
            break
        }

        for (product in list) {
            if (OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitStockId)
                && OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitPurchaseId)
            ) continue
            fetchQuantityUnits()
            break
        }

        for (product in list) {
            if (OBJECTS_PRODUCT_GROUPS.containsKey(product._productGroupId))
                product.productGroup = OBJECTS_PRODUCT_GROUPS[product._productGroupId]!!
            if (OBJECTS_LOCATIONS.containsKey(product._locationId))
                product.location = OBJECTS_LOCATIONS[product._locationId]!!
            if (OBJECTS_LOCATIONS.containsKey(product._shoppingLocationId))
                product.shoppingLocation = OBJECTS_LOCATIONS[product._shoppingLocationId]!!
            if (OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitPurchaseId))
                product.quantityUnitPurchase =
                    OBJECTS_QUANTITY_UNITS[product._quantityUnitPurchaseId]!!
            if (OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitStockId))
                product.quantityUnitStock = OBJECTS_QUANTITY_UNITS[product._quantityUnitStockId]!!
        }

        return list
    }

    private fun fetchQuantityUnits(): List<GrocyQuantityUnit> {
        val list = ArrayList<GrocyQuantityUnit>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/quantity_units").body!!.string())

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            i++

            if (OBJECTS_QUANTITY_UNITS.containsKey(id)) {
                OBJECTS_QUANTITY_UNITS[id]!!.parse(obj)
                list.add(OBJECTS_QUANTITY_UNITS[id]!!)
                continue
            }

            OBJECTS_QUANTITY_UNITS[id] = GrocyQuantityUnit(obj)
            list.add(OBJECTS_QUANTITY_UNITS[id]!!)
        }

        return list
    }

    fun fetchProductGroups(): List<GrocyProductGroup> {
        val list = ArrayList<GrocyProductGroup>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/product_groups").body!!.string())

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            i++

            if (OBJECTS_PRODUCT_GROUPS.containsKey(id)) {
                OBJECTS_PRODUCT_GROUPS[id]!!.parse(obj)
                list.add(OBJECTS_PRODUCT_GROUPS[id]!!)
                continue
            }

            OBJECTS_PRODUCT_GROUPS[id] = GrocyProductGroup(obj)
            list.add(OBJECTS_PRODUCT_GROUPS[id]!!)
        }

        return list
    }

    private fun fetchLocations(): List<GrocyLocation> {
        val list = ArrayList<GrocyLocation>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/locations").body!!.string())

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            i++

            if (OBJECTS_LOCATIONS.containsKey(id)) {
                OBJECTS_LOCATIONS[id]!!.parse(obj)
                list.add(OBJECTS_LOCATIONS[id]!!)
                continue
            }

            OBJECTS_LOCATIONS[id] = GrocyLocation(obj)
            list.add(OBJECTS_LOCATIONS[id]!!)
        }

        return list
    }

}
