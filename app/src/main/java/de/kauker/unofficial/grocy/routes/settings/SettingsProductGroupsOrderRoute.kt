package de.kauker.unofficial.grocy.routes.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.*
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.sdk.grocy.models.GrocyProductGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun SettingsProductGroupsOrderRoute(mainVM: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val vm = mainVM.vmSettingsProductGroupsOrderRoute
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.load() }

    if (!vm.loaded && !vm.connectionIssue) {
        CircularProgressIndicator()
    } else {
        val focusRequester = rememberActiveFocusRequester()

        ScalingLazyColumn(
            modifier = Modifier
                .focusRequester(focusRequester)
                .rotaryWithScroll(focusRequester, sc.scrollableState),
            state = sc.scrollableState,
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

            if (!vm.connectionIssue) {
                item {
                    CompactChip(
                        label = { Text(stringResource(id = R.string.reset)) },
                        icon = { Icon(Icons.Rounded.Refresh, stringResource(id = R.string.reset)) },
                        onClick = { vm.resetOrder() }
                    )
                }

                items(vm.productGroupList.size) {
                    val group = vm.productGroupList[it]

                    Card(
                        onClick = { }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f, true),
                                text = group.name
                            )

                            Row(
                                Modifier.weight(1f, false)
                            ) {
                                CompactButton(
                                    backgroundPadding = 0.dp,
                                    onClick = {
                                        coroutineScope.launch {
                                            sc.scrollableState.scrollToItem(it + 1, 0)
                                        }

                                        vm.moveUpwards(it)
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowUpward,
                                        stringResource(id = R.string.move_up)
                                    )
                                }

                                CompactButton(
                                    modifier = Modifier.padding(start = 4.dp),
                                    colors = ButtonDefaults.secondaryButtonColors(),
                                    backgroundPadding = 0.dp,
                                    onClick = {
                                        coroutineScope.launch {
                                            sc.scrollableState.scrollToItem(it + 3, 0)
                                        }

                                        vm.moveDownwards(it)
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
                }
            } else if (vm.connectionIssue) {
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

class SettingsProductGroupsOrderViewModel constructor(
    private val vm: MainViewModel,
    application: Application
) : AndroidViewModel(
    application
) {

    val productGroupList = mutableStateListOf<GrocyProductGroup>()

    var loaded by mutableStateOf(false)
    var connectionIssue by mutableStateOf(false)

    private var settingsSp: SharedPreferences = getApplication<Application>().getSharedPreferences(
        "settings",
        ComponentActivity.MODE_PRIVATE
    )

    fun load() {
        viewModelScope.launch {
            loaded = false
            connectionIssue = false
            fetchProductGroups()
        }
    }

    private suspend fun fetchProductGroups() {
        withContext(Dispatchers.IO) {
            try {
                var mProductGroupList = vm.grocyClient.fetchProductGroups(false)

                mProductGroupList = mProductGroupList.sortedBy { it.name }

                val productGroupById = HashMap<String, GrocyProductGroup>()
                for (productGroup in mProductGroupList) productGroupById[productGroup.id] =
                    productGroup

                val order = settingsSp.getString("productGroupOrder", null)
                if (order != null) {
                    val jsonArray = JSONArray(order)

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getString(i)
                        mProductGroupList = mProductGroupList.filter { it.id != item }
                    }

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getString(i)
                        if (!productGroupById.containsKey(item)) continue
                        mProductGroupList = mProductGroupList + productGroupById[item]!!
                    }
                }

                productGroupList.clear()
                productGroupList.addAll(mProductGroupList)

                loaded = true
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                connectionIssue = true
            }
        }
    }

    fun moveUpwards(index: Int) {
        if (index == 0) return
        val group = productGroupList[index]
        productGroupList.removeAt(index)
        productGroupList.add(index - 1, group)

        saveOrder()
    }

    fun moveDownwards(index: Int) {
        if (index == productGroupList.size - 1) return
        val group = productGroupList[index]
        productGroupList.removeAt(index)
        productGroupList.add(index + 1, group)

        saveOrder()
    }

    private fun saveOrder() {
        val orderArray = JSONArray()
        for (productGroup in productGroupList) orderArray.put(productGroup.id)
        settingsSp.edit().putString("productGroupOrder", orderArray.toString()).apply()
    }

    fun resetOrder() {
        settingsSp.edit().remove("productGroupOrder").apply()
        viewModelScope.launch { fetchProductGroups() }
    }

}
