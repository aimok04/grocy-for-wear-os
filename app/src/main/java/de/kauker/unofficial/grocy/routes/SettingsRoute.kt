package de.kauker.unofficial.grocy.routes

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Copyright
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.activities.SetupActivity
import de.kauker.unofficial.grocy.views.RotaryScrollAlert

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun SettingsRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
    val context = LocalContext.current

    var showSignOutAlert by remember { mutableStateOf(false) }
    Dialog(showDialog = showSignOutAlert, onDismissRequest = { showSignOutAlert = false }) {
        AlertSignOut(sc, onClickPrimary = {
            showSignOutAlert = false

            context.getSharedPreferences("credentials", Context.MODE_PRIVATE)
                .edit().remove("apiUrl").remove("apiToken").apply()

            vm.settingsSp.edit().remove("latestUnsupportedVersion").apply()

            context.startActivity(Intent(context, SetupActivity().javaClass))
            if(context is Activity) context.finish()
        }, onClickSecondary = {
            showSignOutAlert = false
        })
    }

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
                text = stringResource(id = R.string.settings_title),
                style = MaterialTheme.typography.title2
                    .copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.ListAlt,
                        stringResource(id = R.string.settings_button_product_groups_order)
                    )
                },
                label = { Text(stringResource(id = R.string.settings_button_product_groups_order)) },
                colors = ChipDefaults.primaryChipColors(),
                onClick = { vm.rootNavController?.navigate("settings/productGroupsOrder") }
            )
        }

        item {
            ToggleChip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                checked = vm.amoledMode,
                onCheckedChange = {
                    vm.amoledMode = !vm.amoledMode
                    vm.settingsSp.edit().putBoolean("amoledMode", vm.amoledMode).apply()
                },
                appIcon = {
                    Icon(
                        Icons.Rounded.DarkMode,
                        stringResource(id = R.string.settings_button_amoled)
                    )
                },
                label = { Text(stringResource(id = R.string.settings_button_amoled)) },
                toggleControl = {
                    Switch(checked = vm.amoledMode)
                }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.Rounded.Info,
                        stringResource(id = R.string.settings_button_about_server)
                    )
                },
                label = { Text(stringResource(id = R.string.settings_button_about_server)) },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = { vm.rootNavController?.navigate("settings/aboutServer") }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.Rounded.Copyright,
                        stringResource(id = R.string.settings_button_legal_stuff)
                    )
                },
                label = { Text(stringResource(id = R.string.settings_button_legal_stuff)) },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = { vm.rootNavController?.navigate("settings/legal") }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.Logout,
                        stringResource(id = R.string.sign_out)
                    )
                },
                label = { Text(stringResource(id = R.string.sign_out)) },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = { showSignOutAlert = true }
            )
        }
    }
}

@Composable
fun AlertSignOut(
    sc: ScaffoldContext<ScalingLazyListState>,
    onClickPrimary: () -> Unit,
    onClickSecondary: () -> Unit
) {
    RotaryScrollAlert(
        scrollState = sc.scrollableState,
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.Logout,
                contentDescription = stringResource(id = R.string.sign_out),
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
                text = stringResource(id = R.string.dialog_logout_message),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        },
    ) {
        item {
            Row {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = onClickPrimary,
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Icon(Icons.Rounded.Check, stringResource(id = R.string.yes))
                }

                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = onClickSecondary,
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Icon(Icons.Rounded.Close, stringResource(id = R.string.no))
                }
            }
        }
    }
}
