package de.kauker.unofficial.sdk.grocy

import android.content.Context
import de.kauker.unofficial.sdk.grocy.models.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import okhttp3.OkHttpClient
import org.json.JSONArray

class GrocyClient(
    context: Context,
    val serverUrl: String,
    val apiToken: String
) {

    val OBJECTS_LOCATIONS = HashMap<String, GrocyLocation>()
    val OBJECTS_PRODUCT_GROUPS = HashMap<String, GrocyProductGroup>()
    val OBJECTS_QUANTITY_UNITS = HashMap<String, GrocyQuantityUnit>()

    val OBJECTS_SHOPPING_LISTS = HashMap<Number, GrocyShoppingList>()
    val OBJECTS_PRODUCTS = HashMap<String, GrocyProduct>()
    val OBJECTS_SHOPPING_LIST_ENTRIES = HashMap<String, GrocyShoppingListEntry>()

    val okHttpClient: OkHttpClient = OkHttpClient()
    val cache = context.getSharedPreferences("grocyCache", Context.MODE_PRIVATE)

    fun fetchCacheDate() : Date? {
        val time = cache.getLong("latestCacheDate", 0L)
        return if(time != 0L) Date(time) else null
    }

    fun fetchShoppingLists(cached: Boolean): ArrayList<GrocyShoppingList> {
        val list = ArrayList<GrocyShoppingList>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/shopping_lists", cached))

        var i = 0
        while (!json.isNull(i)) {
            val obj = json.getJSONObject(i)
            val id = obj.getInt("id")
            i++

            if (OBJECTS_SHOPPING_LISTS.containsKey(id)) {
                OBJECTS_SHOPPING_LISTS[id]!!.parse(obj)
                list.add(OBJECTS_SHOPPING_LISTS[id]!!)
                continue
            }

            OBJECTS_SHOPPING_LISTS[id] = GrocyShoppingList(obj)
            list.add(OBJECTS_SHOPPING_LISTS[id]!!)
        }

        return list
    }

    fun fetchShoppingListEntries(cached: Boolean): ArrayList<GrocyShoppingListEntry> {
        val list = ArrayList<GrocyShoppingListEntry>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/shopping_list", cached))

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
            fetchProducts(cached)
            break
        }

        for (entry in list) {
            if (OBJECTS_QUANTITY_UNITS.containsKey(entry._quantityUnitId)) continue
            fetchQuantityUnits(cached)
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

    private fun fetchProducts(cached: Boolean): List<GrocyProduct> {
        val list = ArrayList<GrocyProduct>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/products", cached))

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

            OBJECTS_PRODUCTS[id] = GrocyProduct(this, obj)
            list.add(OBJECTS_PRODUCTS[id]!!)
        }

        for (product in list) {
            if (OBJECTS_LOCATIONS.containsKey(product._locationId)
                && OBJECTS_LOCATIONS.containsKey(product._shoppingLocationId)
            ) continue
            fetchLocations(cached)
            break
        }

        for (product in list) {
            if (OBJECTS_PRODUCT_GROUPS.containsKey(product._productGroupId)) continue
            fetchProductGroups(cached)
            break
        }

        for (product in list) {
            if (OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitStockId)
                && OBJECTS_QUANTITY_UNITS.containsKey(product._quantityUnitPurchaseId)
            ) continue
            fetchQuantityUnits(cached)
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

    private fun fetchQuantityUnits(cached: Boolean): List<GrocyQuantityUnit> {
        val list = ArrayList<GrocyQuantityUnit>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/quantity_units", cached))

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

    fun fetchProductGroups(cached: Boolean): List<GrocyProductGroup> {
        val list = ArrayList<GrocyProductGroup>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/product_groups", cached))

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

    private fun fetchLocations(cached: Boolean): List<GrocyLocation> {
        val list = ArrayList<GrocyLocation>()
        val json = JSONArray(GrocyRequest(this).get("/api/objects/locations", cached))

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
