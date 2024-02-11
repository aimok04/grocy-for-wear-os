package de.kauker.unofficial.grocy.routes.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Support
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R

@Composable
fun AlertUnsupportedRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val serverVersion = vm.grocySystemInfo?.grocyVersion?.version?: "Unknown"

    Alert(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        scrollState = sc.scrollableState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = {
            Icon(
                Icons.Rounded.Support,
                contentDescription = stringResource(id = R.string.dialog_unsupported_title),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_unsupported_title),
                textAlign = TextAlign.Center
            )
        },
        message = {
            Text(
                text = stringResource(id = R.string.dialog_unsupported_message, serverVersion),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        },
    ) {
        item {
            Row {
                CompactChip(
                    label = { Text(stringResource(id = R.string.close)) },
                    icon = { Icon(Icons.Rounded.Close, stringResource(id = R.string.close)) },
                    onClick = {
                        vm.settingsSp.edit().putString("latestUnsupportedVersion", serverVersion).apply()
                        vm.rootNavController?.popBackStack()
                    }
                )
            }
        }
    }
}
