package de.kauker.unofficial.grocy

import android.app.Application
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.kauker.unofficial.grocy.models.ShoppingListEntry
import de.kauker.unofficial.grocy.models.ShoppingListGrocyItemEntry
import de.kauker.unofficial.grocy.models.ShoppingListTitleEntry
import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.models.GrocyProductGroup
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainViewModel constructor(apiUrl: String, apiToken: String, application: Application) :
    AndroidViewModel(
        application
    ) {

    class StateData {
        var shoppingListItems = ArrayList<ShoppingListEntry>()
    }

    sealed class State {
        data class Data(val data: StateData?, val id: Double) : State()
        object Loading : State()
        object ConnectionIssue : State()
    }

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var stateData = StateData()
    private var shoppingListEntries = ArrayList<GrocyShoppingListEntry>()

    private var settingsSp: SharedPreferences = getApplication<Application>().getSharedPreferences(
        "settings",
        ComponentActivity.MODE_PRIVATE
    )

    private var grocyClient = GrocyClient(apiUrl, apiToken)

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.emit(State.Loading)

            withContext(Dispatchers.IO) {
                try {
                    shoppingListEntries = grocyClient.fetchShoppingListEntries()
                    reloadUi()
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    _state.emit(State.ConnectionIssue)
                }
            }
        }
    }

    private fun reloadUi() {
        stateData.shoppingListItems.clear()

        val sectionsMap = HashMap<GrocyProductGroup, ArrayList<GrocyShoppingListEntry>>()
        val unsorted = ArrayList<GrocyShoppingListEntry>()
        val done = ArrayList<GrocyShoppingListEntry>()

        for (entry in shoppingListEntries) {
            if (entry.done) {
                done.add(entry)
                continue
            }

            if (entry.product!!.productGroup == null) {
                unsorted.add(entry)
                continue
            }

            if (!sectionsMap.containsKey(entry.product!!.productGroup))
                sectionsMap[entry.product!!.productGroup!!] = ArrayList()

            sectionsMap[entry.product!!.productGroup!!]!!.add(entry)
        }

        /* sorting by alphabet and applying custom order if available */
        val groupMap = HashMap<String, GrocyProductGroup>()
        for (group in sectionsMap.keys) groupMap[group.id] = group

        var groupOrder = sectionsMap.keys.toList()
        groupOrder = groupOrder.sortedBy { it.name }

        val order = settingsSp.getString("productGroupOrder", null)
        if (order != null) {
            val jsonArray = JSONArray(order)

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getString(i)
                groupOrder = groupOrder.filter { it.id != item }
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getString(i)
                if (!groupMap.containsKey(item)) continue
                groupOrder = groupOrder + groupMap[item]!!
            }
        }

        for (group in groupOrder) {
            stateData.shoppingListItems.add(ShoppingListTitleEntry(title = group.name))
            for (entry in sectionsMap[group]!!) stateData.shoppingListItems.add(
                ShoppingListGrocyItemEntry(
                    entry
                )
            )
        }

        if (unsorted.size != 0) {
            stateData.shoppingListItems.add(ShoppingListTitleEntry(titleId = R.string.main_list_unsorted))
            for (entry in unsorted) stateData.shoppingListItems.add(ShoppingListGrocyItemEntry(entry))
        }

        if (done.size != 0) {
            stateData.shoppingListItems.add(ShoppingListTitleEntry(titleId = R.string.main_list_done))
            for (entry in done) stateData.shoppingListItems.add(ShoppingListGrocyItemEntry(entry))
        }

        viewModelScope.launch {
            _state.emit(State.Data(stateData, Math.random()))
        }
    }

    fun toggleShoppingListEntryDoneStatus(entry: GrocyShoppingListEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val prevStatus = entry.done
                entry.done = !prevStatus
                reloadUi()

                if (entry.setDone(!prevStatus)) return@withContext

                entry.done = prevStatus
                reloadUi()
            }
        }
    }

}
