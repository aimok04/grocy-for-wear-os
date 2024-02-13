package de.kauker.unofficial.grocy.routes.alerts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SyncAlt
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
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.views.RotaryScrollAlert

@Composable
fun AlertOfflineRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    RotaryScrollAlert(
        scrollState = sc.scrollableState,
        icon = {
            Icon(
                Icons.Rounded.SyncAlt,
                contentDescription = stringResource(id = R.string.dialog_offline_title),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_offline_title),
                textAlign = TextAlign.Center
            )
        },
        message = {
            Text(
                text = stringResource(R.string.dialog_offline_message),
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
                    onClick = { vm.rootNavController?.popBackStack() }
                )
            }
        }
    }
}
