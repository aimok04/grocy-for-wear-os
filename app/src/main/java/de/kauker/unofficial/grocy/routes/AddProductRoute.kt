package de.kauker.unofficial.grocy.routes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Confirmation
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.theme.Typography
import de.kauker.unofficial.grocy.utils.distanceTo
import de.kauker.unofficial.grocy.views.TextInput
import de.kauker.unofficial.sdk.grocy.models.GrocyProduct
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun AddProductRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    var showDoneDialog by remember { mutableStateOf(false) }

    if(showDoneDialog) {
        Confirmation(
            durationMillis = 1000L,
            onTimeout = { vm.rootNavController?.popBackStack() },
            icon = { Icon(Icons.Rounded.Check, stringResource(id = R.string.done)) }
        ) {
            Text(stringResource(id = R.string.done))
        }

        return
    }

    /* first step: type name */
    var text by remember { mutableStateOf<CharSequence>("") }

    if(text.isEmpty()) {
        TextInput(
            label = stringResource(R.string.add_product_route_product_search_label),
            onTextReceived = { text = it },
            onDismiss = { vm.rootNavController?.popBackStack() }
        )

        return
    }

    /* third step: choose amount */
    var selectedProduct by remember { mutableStateOf<GrocyProduct?>(null) }

    if(selectedProduct != null) {
        var unitLabelWidth by remember { mutableStateOf(0.dp) }

        Box(
            Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val focusRequester = rememberActiveFocusRequester()

            ScalingLazyColumn(
                modifier = Modifier
                    .offset(x = -(unitLabelWidth))
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .rotaryWithScroll(focusRequester, sc.scrollableState),
                state = sc.scrollableState,
                flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = sc.scrollableState)
            ) {
                item {
                    Spacer(Modifier.height((configuration.screenHeightDp / 3).dp))
                }

                items(98) {
                    val value = it + 1

                    Text(
                        text = value.toString(),
                        color = if(sc.scrollableState.centerItemIndex != value) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current,
                        style = MaterialTheme.typography.display2
                    )
                }
            }

            Text(
                modifier = Modifier
                    .offset(x = unitLabelWidth)
                    .onGloballyPositioned {
                        if (unitLabelWidth > 0.dp) return@onGloballyPositioned
                        with(density) { unitLabelWidth = (it.size.width.toDp() + 12.dp) / 2 }
                    },
                text = (if(sc.scrollableState.centerItemIndex == 1) selectedProduct?.quantityUnitPurchase?.name else selectedProduct?.quantityUnitPurchase?.namePlural)?: "x",
                style = Typography.title1
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CompactChip(
                modifier = Modifier.padding(bottom = 12.dp),
                icon = { Icon(Icons.Rounded.Done, stringResource(R.string.done)) },
                label = { Text(stringResource(R.string.finish)) },
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            if(!selectedProduct!!.addToShoppingList(vm.vmHomeRoute.selectedShoppingList?.id?.toInt()?: 1, sc.scrollableState.centerItemIndex)) return@withContext
                            showDoneDialog = true
                        }
                    }
                }
            )
        }
        return
    }

    /* second step: choose products */
    val suggestedProducts = remember { mutableStateListOf<Pair<GrocyProduct, Double>>() }
    LaunchedEffect(text) {
        val search = text.toString()
        val searchLower = search.lowercase()

        vm.grocyClient.OBJECTS_PRODUCTS.entries.forEach fE@ {
            val values: Array<String> = arrayOf(it.value.name, it.value.description, it.value.productGroup?.name?: "", it.value.location?.name?: "")
            values.forEach { c1 ->
                val diff = c1.distanceTo(search)
                if(diff > 0.6) {
                    suggestedProducts.add(Pair(it.value, diff))
                    return@fE
                }

                if(c1.lowercase().contains(searchLower)) {
                    suggestedProducts.add(Pair(it.value, diff))
                    return@fE
                }
            }
        }

        try {
            suggestedProducts.sortBy { it.second }
            suggestedProducts.reverse()
        }catch(e: Exception) {
            e.printStackTrace()
        }
    }

    val focusRequester = rememberActiveFocusRequester()
    ScalingLazyColumn(
        modifier = Modifier
            .focusRequester(focusRequester)
            .rotaryWithScroll(focusRequester, sc.scrollableState),
        state = sc.scrollableState
    ) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                text = stringResource(id = R.string.add_product_route_products_found_title),
                style = MaterialTheme.typography.title2
                    .copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }

        if(suggestedProducts.size == 0) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.add_product_route_no_products_found_label),
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        }else{
            items(suggestedProducts.size) {
                val product = suggestedProducts[it]

                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Icon(Icons.Rounded.Add, stringResource(id = R.string.add)) },
                    label = { Text(product.first.name) },
                    secondaryLabel = { Text((product.second * 100).roundToInt().toString() + "%") },
                    onClick = { selectedProduct = product.first }
                )
            }
        }
    }
}
