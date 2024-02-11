package de.kauker.unofficial.grocy.routes.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.MainViewModel
import de.kauker.unofficial.grocy.R

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun SettingsAboutServerRoute(vm: MainViewModel, sc: ScaffoldContext<ScalingLazyListState>) {
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
                text = stringResource(id = R.string.settings_button_about_server),
                style = MaterialTheme.typography.title2
                    .copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.grocyVersion?.version?: "Unknown") },
                secondaryLabel = { Text("Grocy version") },
                colors = ChipDefaults.primaryChipColors(),
                onClick = {  }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.grocyVersion?.releaseDate?: "Unknown") },
                secondaryLabel = { Text("Release date") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {  }
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.phpVersion?: "Unknown") },
                secondaryLabel = { Text("PHP version") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {  }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.sqlliteVersion?: "Unknown") },
                secondaryLabel = { Text("SqlLite version") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {  }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.os?: "Unknown") },
                secondaryLabel = { Text("Operating system") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {  }
            )
        }

        item {
            Chip(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                label = { Text(vm.grocySystemInfo?.client?: "Unknown") },
                secondaryLabel = { Text("Client") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {  }
            )
        }
    }
}
