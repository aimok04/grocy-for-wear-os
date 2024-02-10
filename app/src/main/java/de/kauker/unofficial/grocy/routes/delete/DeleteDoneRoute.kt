package de.kauker.unofficial.grocy.routes.delete

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Confirmation
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DeleteDoneRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
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

    Alert(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        scrollState = sc.scrollableState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(id = R.string.delete),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_confirmation_title),
                textAlign = TextAlign.Center
            )
        },
        message = {
            Text(
                text = stringResource(R.string.dialog_delete_done_prompt_message),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        },
    ) {
        item {
            Row {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                vm.vmHomeRoute.deleteDoneEntries()
                                showDoneDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(Icons.Rounded.Check, stringResource(id = R.string.yes))
                }

                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        vm.rootNavController?.popBackStack()
                    },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(Icons.Rounded.Close, stringResource(id = R.string.no))
                }
            }
        }
    }
}
