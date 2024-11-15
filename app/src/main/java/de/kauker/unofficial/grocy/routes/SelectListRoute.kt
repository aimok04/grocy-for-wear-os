package de.kauker.unofficial.grocy.routes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.views.RotaryScrollAlert

@Composable
fun SelectListRoute(mainVM: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val vm = mainVM.vmHomeRoute

    RotaryScrollAlert(
        scrollState = sc.scrollableState,
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.ListAlt,
                contentDescription = stringResource(id = R.string.select_list_route_title),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.select_list_route_title),
                textAlign = TextAlign.Center
            )
        }
    ) {
        items(vm.shoppingLists.size) {
            val list = vm.shoppingLists[it]

            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = list == vm.selectedShoppingList,
                onCheckedChange = {
                    vm.vm.settingsSp.edit().putInt("selectedShoppingListId", list.id.toInt())
                        .apply()

                    vm.selectedShoppingList = list
                    vm.vm.rootNavController?.popBackStack()
                },
                label = { Text(list.name ?: "Unnamed") },
                toggleControl = {
                    RadioButton(
                        selected = list == vm.selectedShoppingList
                    )
                }
            )
        }
    }
}
