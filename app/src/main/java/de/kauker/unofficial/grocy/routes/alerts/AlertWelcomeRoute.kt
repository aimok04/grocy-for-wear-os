package de.kauker.unofficial.grocy.routes.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import de.kauker.unofficial.grocy.GOOGLE_PLAY_VERSION
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R

@Composable
fun AlertWelcomeRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    Alert(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        scrollState = sc.scrollableState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = {
            Icon(
                Icons.Rounded.WavingHand,
                contentDescription = stringResource(id = R.string.delete),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_welcome_title),
                textAlign = TextAlign.Center
            )
        },
        message = {
            Text(
                text = stringResource(if(GOOGLE_PLAY_VERSION) R.string.dialog_welcome_google_play_message else R.string.dialog_welcome_foss_message),
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