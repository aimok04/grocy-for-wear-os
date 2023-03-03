package de.kauker.unofficial.grocy.activities

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.wear.compose.material.*
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.theme.WearAppTheme
import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.models.GrocyProductGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class ProductGroupsOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        if (!sp.contains("apiUrl") || !sp.contains("apiToken")) {
            startActivity(Intent(this, SetupActivity().javaClass))
            finish()
            return
        }

        val viewModel =
            ProductGroupsOrderViewModel(
                sp.getString("apiUrl", "")!!,
                sp.getString("apiToken", "")!!,
                application
            )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    setContent {
                        WearAppTheme {
                            ProductGroupsOrderComp(
                                state,
                                viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun ProductGroupsOrderComp(
    state: ProductGroupsOrderViewModel.State,
    viewModel: ProductGroupsOrderViewModel
) {
    val scalingLazyListState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    if (state is ProductGroupsOrderViewModel.State.Loading) {
        CircularProgressIndicator()
    } else {
        val focusRequester = rememberActiveFocusRequester()

        ScalingLazyColumn(
            modifier = Modifier.focusRequester(focusRequester)
                .rotaryWithScroll(focusRequester, scalingLazyListState),
            state = scalingLazyListState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp),
                    text = stringResource(id = R.string.productgroups_title),
                    style = MaterialTheme.typography.title2
                        .copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }

            if (state is ProductGroupsOrderViewModel.State.Data) {
                item {
                    Chip(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        label = { Text(stringResource(id = R.string.reset)) },
                        colors = ChipDefaults.secondaryChipColors(),
                        onClick = { viewModel.resetOrder() }
                    )
                }

                items(state.data!!.productGroupList!!.size) {
                    val group = state.data.productGroupList!![it]

                    TitleCard(
                        title = { Text(group.name) },
                        onClick = { }
                    ) {
                        Row {
                            Button(
                                colors = ButtonDefaults.iconButtonColors(),
                                onClick = {
                                    coroutineScope.launch {
                                        scalingLazyListState.scrollToItem(it + 1, 0)
                                    }

                                    viewModel.moveUpwards(it)
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowUpward,
                                    stringResource(id = R.string.move_up)
                                )
                            }

                            Button(
                                colors = ButtonDefaults.iconButtonColors(),
                                onClick = {
                                    coroutineScope.launch {
                                        scalingLazyListState.scrollToItem(it + 3, 0)
                                    }

                                    viewModel.moveDownwards(it)
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowDownward,
                                    stringResource(id = R.string.move_down)
                                )
                            }
                        }
                    }
                }
            } else if (state is ProductGroupsOrderViewModel.State.ConnectionIssue) {
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(id = R.string.main_prompt_connection_issues),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

class ProductGroupsOrderViewModel constructor(
    apiUrl: String,
    apiToken: String,
    application: Application
) : AndroidViewModel(
    application
) {

    class StateData {
        var productGroupList: MutableList<GrocyProductGroup>? = null
    }

    sealed class State {
        data class Data(val data: StateData?, val id: Double) : State()
        object Loading : State()
        object ConnectionIssue : State()
    }

    private var _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var stateData = StateData()
    private var settingsSp: SharedPreferences = getApplication<Application>().getSharedPreferences(
        "settings",
        ComponentActivity.MODE_PRIVATE
    )

    private var grocyClient = GrocyClient(application, apiUrl, apiToken)

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.emit(State.Loading)
            fetchProductGroups()
        }
    }

    private suspend fun fetchProductGroups() {
        withContext(Dispatchers.IO) {
            try {
                var productGroupList = grocyClient.fetchProductGroups(false)

                productGroupList = productGroupList.sortedBy { it.name }

                val productGroupById = java.util.HashMap<String, GrocyProductGroup>()
                for (productGroup in productGroupList) productGroupById[productGroup.id] =
                    productGroup

                val order = settingsSp.getString("productGroupOrder", null)
                if (order != null) {
                    val jsonArray = JSONArray(order)

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getString(i)
                        productGroupList = productGroupList.filter { it.id != item }
                    }

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getString(i)
                        if (!productGroupById.containsKey(item)) continue
                        productGroupList = productGroupList + productGroupById[item]!!
                    }
                }

                stateData.productGroupList = productGroupList.toMutableList()
                _state.emit(State.Data(stateData, Math.random()))
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                _state.emit(State.ConnectionIssue)
            }
        }
    }

    fun moveUpwards(index: Int) {
        if (index == 0) return
        val group = stateData.productGroupList!![index]
        stateData.productGroupList!!.removeAt(index)
        stateData.productGroupList!!.add(index - 1, group)

        reloadUI()
        saveOrder()
    }

    fun moveDownwards(index: Int) {
        if (index == stateData.productGroupList!!.size - 1) return
        val group = stateData.productGroupList!![index]
        stateData.productGroupList!!.removeAt(index)
        stateData.productGroupList!!.add(index + 1, group)

        reloadUI()
        saveOrder()
    }

    private fun reloadUI() {
        viewModelScope.launch { _state.emit(State.Data(stateData, Math.random())) }
    }

    private fun saveOrder() {
        val orderArray = JSONArray()
        for (productGroup in stateData.productGroupList!!) orderArray.put(productGroup.id)
        settingsSp.edit().putString("productGroupOrder", orderArray.toString()).apply()
    }

    fun resetOrder() {
        settingsSp.edit().remove("productGroupOrder").apply()
        viewModelScope.launch { fetchProductGroups() }
    }

}
