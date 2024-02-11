package de.kauker.unofficial.grocy.routes

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
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

            /* display transaction syncing info */
            if(vm.transactionSyncTotalCount != 0) item {
                val progressAnimatable = remember { Animatable(0f) }
                val finishedAnimatable = remember { Animatable(0f) }
                val closeAnimatable = remember { Animatable(1f) }

                suspend fun endAnimation() {
                    finishedAnimatable.animateTo(2f, tween(durationMillis = 500))
                    closeAnimatable.animateTo(0f, tween(delayMillis = 1000, durationMillis = 500))

                    vm.transactionSyncFailed = false
                    vm.transactionSyncCompletedCount = 0
                    vm.transactionSyncTotalCount = 0
                }

                LaunchedEffect(vm.transactionSyncFailed) {
                    progressAnimatable.animateTo(0f)
                    endAnimation()
                }

                LaunchedEffect(vm.transactionSyncCompletedCount, vm.transactionSyncTotalCount) {
                    if(vm.transactionSyncFailed) return@LaunchedEffect

                    val newValue = vm.transactionSyncCompletedCount.toFloat() / vm.transactionSyncTotalCount.toFloat()
                    progressAnimatable.animateTo(newValue, tween(durationMillis = 250))

                    if(newValue != 1f) return@LaunchedEffect
                    endAnimation()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(closeAnimatable.value),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        indicatorColor = if(vm.transactionSyncFailed) MaterialTheme.colors.error else MaterialTheme.colors.primary,
                        progress = progressAnimatable.value
                    )

                    Icon(
                        modifier = Modifier.scale((1f - finishedAnimatable.value).takeIf { it > 0f }?: 0f),
                        imageVector = Icons.Rounded.SyncAlt,
                        contentDescription = stringResource(id = R.string.main_sync_ongoing)
                    )

                    if(vm.transactionSyncFailed) {
                        Icon(
                            modifier = Modifier.scale((finishedAnimatable.value - 1f).takeIf { it > 0f }?: 0f),
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(id = R.string.main_sync_failed),
                            tint = MaterialTheme.colors.error
                        )
                    }else{
                        Icon(
                            modifier = Modifier.scale((finishedAnimatable.value - 1f).takeIf { it > 0f }?: 0f),
                            imageVector = Icons.Rounded.Done,
                            contentDescription = stringResource(id = R.string.main_sync_done),
                            tint = MaterialTheme.colors.primary
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListEntryCard(vm: HomeViewModel, item: ShoppingListGrocyItemEntry) {
    val density = LocalDensity.current

    val entry = item.entry
    val alpha = if (entry.done && !vm.vm.ambientMode) 0.5f else 1f

    var showMoreOptionsOverlay by remember { mutableStateOf(false) }

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
            var size by remember { mutableStateOf(DpSize.Zero) }

            TitleCard(
                modifier = Modifier
                    .alpha(alpha)
                    .onGloballyPositioned {
                        size = with(density) { DpSize(it.size.width.toDp(), it.size.height.toDp()) }
                    },
                title = { Text(entry.product?.name?: entry.note?: "Unknown item") },
                backgroundPainter =
                    if(vm.vm.ambientMode) CardDefaults.cardBackgroundPainter(Color.Black, Color.Black) else CardDefaults.cardBackgroundPainter(),
                onClick = { },
                enabled = false
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

            /* click target */
            Box(
                Modifier
                    .size(size)
                    .clip(MaterialTheme.shapes.large)
                    .background(if (showMoreOptionsOverlay) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
                    .combinedClickable(onLongClick = {
                        showMoreOptionsOverlay = !showMoreOptionsOverlay
                    }) {
                        if (showMoreOptionsOverlay) {
                            showMoreOptionsOverlay = false
                            return@combinedClickable
                        }

                        vm.toggleShoppingListEntryDoneStatus(entry)
                    },
                contentAlignment = Alignment.Center
            ) {
                if(!showMoreOptionsOverlay) return

                CompactButton(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error, contentColor = MaterialTheme.colors.onError),
                    onClick = { vm.deleteShoppingListEntry(item.entry); showMoreOptionsOverlay = false }
                ) {
                    Icon(Icons.Rounded.Delete, stringResource(id = R.string.delete))
                }
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

    var transactionSyncFailed by mutableStateOf(false)
    var transactionSyncTotalCount by mutableIntStateOf(0)
    var transactionSyncCompletedCount by mutableIntStateOf(0)

    var cachedDate by mutableStateOf<Date?>(null)

    fun load(autoRefresh: Boolean = false) {
        viewModelScope.launch {
            if(autoRefresh) {
                withContext(Dispatchers.IO) {
                    try {
                        vm.grocyClient.fetchShoppingListEntries(false)
                        vm.grocyClient.transactionsManager.applyAll(false)

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
                    vm.grocyClient.fetchShoppingLists(true)
                    vm.grocyClient.fetchShoppingListEntries(true)

                    vm.grocyClient.transactionsManager.applyAll(true)

                    viewModelScope.launch {
                        delay(1000)
                        if(!loaded) {
                            reloadUi()
                            cachedDate = vm.grocyClient.fetchCacheDate()
                        }
                    }

                    try {
                        vm.grocyClient.fetchShoppingLists(false)
                        vm.grocyClient.fetchShoppingListEntries(false)

                        vm.grocyClient.transactionsManager.applyAll(false)
                        reloadUi()

                        vm.grocyClient.transactionsManager.flushAll { failed, total, completed ->
                            transactionSyncFailed = failed
                            transactionSyncTotalCount = total
                            transactionSyncCompletedCount = completed
                        }

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
        shoppingLists.addAll(vm.grocyClient.shoppingLists)

        /* get selected list, replaced with first list if its null */
        selectedShoppingList = vm.grocyClient.OBJECTS_SHOPPING_LISTS[vm.settingsSp.getInt("selectedShoppingListId", -1)]?: shoppingLists[0]

        val mShoppingListItems = mutableListOf<ShoppingListEntry>()

        val sectionsMap = HashMap<GrocyProductGroup, ArrayList<GrocyShoppingListEntry>>()
        val unsorted = ArrayList<GrocyShoppingListEntry>()
        val done = ArrayList<GrocyShoppingListEntry>()

        for (entry in vm.grocyClient.shoppingListEntries) {
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

    fun deleteShoppingListEntry(entry: GrocyShoppingListEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                entry.delete()
                reloadUi()
            }
        }
    }

    suspend fun deleteDoneEntries(): Boolean {
        shoppingListItems.forEach {
            if(it !is ShoppingListGrocyItemEntry) return@forEach
            if(!it.entry.done) return@forEach

            it.entry.delete()
        }

        return true
    }

}
