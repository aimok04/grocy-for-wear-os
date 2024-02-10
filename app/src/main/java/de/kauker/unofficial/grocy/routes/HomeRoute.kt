package de.kauker.unofficial.grocy.routes

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.models.ShoppingListEntry
import de.kauker.unofficial.grocy.models.ShoppingListGrocyItemEntry
import de.kauker.unofficial.grocy.models.ShoppingListTitleEntry
import de.kauker.unofficial.sdk.grocy.models.GrocyProductGroup
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingList
import de.kauker.unofficial.sdk.grocy.models.GrocyShoppingListEntry
import de.kauker.unofficial.sdk.grocy.transactions.sub.delete
import de.kauker.unofficial.sdk.grocy.transactions.sub.done
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun HomeRoute(mainVM: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val vm = mainVM.vmHomeRoute

    LaunchedEffect(Unit) {
        vm.load()

        while(true) {
            delay(10000)
            vm.load(true)
        }
    }

    LaunchedEffect(Unit) {
        if(mainVM.settingsSp.getBoolean("wasWelcomed", false)) return@LaunchedEffect
        mainVM.rootNavController?.navigate("alerts/welcome")

        mainVM.settingsSp.edit().putBoolean("wasWelcomed", true).apply()
    }

    if (!vm.loaded && !vm.connectionIssues) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val focusRequester = rememberActiveFocusRequester()

        ScalingLazyColumn(
            modifier = Modifier
                .focusRequester(focusRequester)
                .rotaryWithScroll(focusRequester, sc.scrollableState),
            state = sc.scrollableState
        ) {
            /* display cache info */
            if(!vm.connectionIssues && vm.cachedDate != null) {
                item {
                    Column {
                        val dateStr = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(vm.cachedDate!!)

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.7f),
                            text = stringResource(id = R.string.main_last_refresh),
                            style = MaterialTheme.typography.caption1,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(0.7f),
                            text = dateStr,
                            style = MaterialTheme.typography.caption3,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            /* display page title */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (!vm.connectionIssues && vm.cachedDate != null) 4.dp else 22.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.clickable { vm.vm.rootNavController?.navigate("selectList") },
                        text = vm.selectedShoppingList?.name?: stringResource(id = R.string.main_title),
                        style = MaterialTheme.typography.title2
                            .copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.width(4.dp))

                    CompactButton(
                        onClick = { vm.vm.rootNavController?.navigate("selectList") },
                        backgroundPadding = 0.dp,
                        colors = ButtonDefaults.iconButtonColors()
                    ) {
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = stringResource(id = R.string.select_list_route_title)
                        )
                    }
                }
            }

            /* display add and settings buttons */
            item {
                Row(
                    Modifier.offset(y = -(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactChip(
                        onClick = { vm.vm.rootNavController?.navigate("add") },
                        icon = { Icon(Icons.Rounded.Add, stringResource(id = R.string.add)) }
                    )

                    Spacer(Modifier.width(8.dp))

                    CompactButton(
                        backgroundPadding = 0.dp,
                        onClick = { vm.vm.rootNavController?.navigate("settings") }
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                }
            }

            if (vm.connectionIssues) {
                /* display connection issue error */
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(id = R.string.main_prompt_connection_issues),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                if(vm.shoppingListItems.size == 0) {
                    /* display no items in list info */
                    item {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(0.7f),
                            text = stringResource(id = R.string.main_list_empty_message),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }else{
                    /* display items in shopping list */
                    items(vm.shoppingListItems.size) {
                        val item = vm.shoppingListItems[it]

                        if (item is ShoppingListTitleEntry) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    text = if (item.title != null) item.title!! else stringResource(
                                        id = item.titleId!!
                                    ),
                                    style = MaterialTheme.typography.body2,
                                    textAlign = TextAlign.Center
                                )

                                if(item.titleId == R.string.main_list_done) {
                                    CompactChip(
                                        icon = { Icon(Icons.Rounded.Delete, stringResource(id = R.string.delete_all)) },
                                        label = { Text(stringResource(id = R.string.delete_all)) },
                                        onClick = { vm.vm.rootNavController?.navigate("delete/done") }
                                    )
                                }
                            }
                        } else if (item is ShoppingListGrocyItemEntry) {
                            ShoppingListEntryCard(item = item, vm = vm)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListEntryCard(vm: HomeViewModel, item: ShoppingListGrocyItemEntry) {
    val entry = item.entry
    val alpha = if (entry.done && !vm.vm.ambientMode) 0.5f else 1f

    Box(
        Modifier.padding(start = 12.dp, end = 12.dp)
    ) {
        Box(
            Modifier
                .background(
                    if (vm.vm.ambientMode) MaterialTheme.colors.onPrimary else Color.Transparent,
                    MaterialTheme.shapes.large
                )
                .padding(1.dp)
        ) {
            TitleCard(
                title = { Text(entry.product?.name?: entry.note?: "Unknown item") },
                backgroundPainter =
                    if(vm.vm.ambientMode) CardDefaults.cardBackgroundPainter(Color.Black, Color.Black) else CardDefaults.cardBackgroundPainter(),
                modifier = Modifier
                    .alpha(alpha),
                onClick = {
                    vm.toggleShoppingListEntryDoneStatus(entry)
                }
            ) {
                if(entry.product != null && entry.note?.isNotEmpty() == true) {
                    Text(
                        entry.note?: "",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                }

                val quantityUnit =
                    if (entry.quantityUnit == null) "" else if (entry.amount == "1") entry.quantityUnit?.name else entry.quantityUnit?.namePlural
                Text(entry.amount + " " + quantityUnit)
            }
        }
    }
}

class HomeViewModel(
    val vm: MainViewModel,
    application: Application
) : AndroidViewModel(application) {

    var loaded by mutableStateOf(false)
    var connectionIssues by mutableStateOf(false)

    var selectedShoppingList by mutableStateOf<GrocyShoppingList?>(null)

    val shoppingListItems = mutableStateListOf<ShoppingListEntry>()
    val shoppingLists = mutableStateListOf<GrocyShoppingList>()

    private var shoppingListEntries = ArrayList<GrocyShoppingListEntry>()
    private var mShoppingLists = ArrayList<GrocyShoppingList>()
    var cachedDate by mutableStateOf<Date?>(null)

    fun load(autoRefresh: Boolean = false) {
        viewModelScope.launch {
            if(autoRefresh) {
                withContext(Dispatchers.IO) {
                    try {
                        shoppingListEntries = vm.grocyClient.fetchShoppingListEntries(false)
                        reloadUi()
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }
                }

                return@launch
            }

            loaded = false
            connectionIssues = false

            withContext(Dispatchers.IO) {
                try {
                    mShoppingLists = vm.grocyClient.fetchShoppingLists(true)
                    shoppingListEntries = vm.grocyClient.fetchShoppingListEntries(true)

                    viewModelScope.launch {
                        delay(1000)
                        if(!loaded) {
                            reloadUi()
                            cachedDate = vm.grocyClient.fetchCacheDate()
                        }
                    }

                    try {
                        mShoppingLists = vm.grocyClient.fetchShoppingLists(false)
                        shoppingListEntries = vm.grocyClient.fetchShoppingListEntries(false)
                        reloadUi()

                        cachedDate = null
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                    connectionIssues = true
                }
            }
        }
    }

    private fun reloadUi() {
        shoppingLists.clear()
        shoppingLists.addAll(mShoppingLists)

        /* get selected list, replaced with first list if its null */
        selectedShoppingList = vm.grocyClient.OBJECTS_SHOPPING_LISTS[vm.settingsSp.getInt("selectedShoppingListId", -1)]?: shoppingLists[0]

        val mShoppingListItems = mutableListOf<ShoppingListEntry>()

        val sectionsMap = HashMap<GrocyProductGroup, ArrayList<GrocyShoppingListEntry>>()
        val unsorted = ArrayList<GrocyShoppingListEntry>()
        val done = ArrayList<GrocyShoppingListEntry>()

        for (entry in shoppingListEntries) {
            if(entry.shoppingListId != selectedShoppingList?.id) continue

            if (entry.done) {
                done.add(entry)
                continue
            }

            if (entry.product?.productGroup == null) {
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

        val order = vm.settingsSp.getString("productGroupOrder", null)
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
            mShoppingListItems.add(ShoppingListTitleEntry(title = group.name?: ""))
            for (entry in sectionsMap[group]!!) mShoppingListItems.add(
                ShoppingListGrocyItemEntry(
                    entry
                )
            )
        }

        if (unsorted.size != 0) {
            mShoppingListItems.add(ShoppingListTitleEntry(titleId = R.string.main_list_unsorted))
            for (entry in unsorted) mShoppingListItems.add(ShoppingListGrocyItemEntry(entry))
        }

        if (done.size != 0) {
            mShoppingListItems.add(ShoppingListTitleEntry(titleId = R.string.main_list_done))
            for (entry in done) mShoppingListItems.add(ShoppingListGrocyItemEntry(entry))
        }

        loaded = true

        shoppingListItems.clear()
        shoppingListItems.addAll(mShoppingListItems)
    }

    fun toggleShoppingListEntryDoneStatus(entry: GrocyShoppingListEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                entry.done(!entry.done)
                reloadUi()
            }
        }
    }

    fun deleteDoneEntries(): Boolean {
        shoppingListItems.forEach {
            if(it !is ShoppingListGrocyItemEntry) return@forEach
            if(!it.entry.done) return@forEach

            it.entry.delete()
        }

        return true
    }

}
