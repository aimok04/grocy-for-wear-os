package de.kauker.unofficial.grocy.routes.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.views.RotaryScrollAlert

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun SettingsLegalRoute(sc: ScaffoldContext<ScalingLazyListState>) {
    val context = LocalContext.current
    var libs by remember { mutableStateOf<Libs?>(null) }

    LaunchedEffect(Unit) {
        libs = Libs.Builder().withContext(context).build()
    }

    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedLibrary: Library? by remember { mutableStateOf(null) }

    if(showDetailsDialog) {
        AlertLegalDetails(selectedLibrary, sc) { showDetailsDialog = false }
        return
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
                text = stringResource(id = R.string.legal_title),
                style = MaterialTheme.typography.title2
                    .copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
        }

        if(libs != null) {
            items(libs!!.libraries.size) {
                val library = libs!!.libraries[it]

                Box(
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colors.surface, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedLibrary = library
                            showDetailsDialog = true
                        }
                ) {
                    Column(
                        Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = library.name + " @ " + library.artifactVersion,
                            style = MaterialTheme.typography.title3,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        val developers = ArrayList<String?>()
                        for (developer in library.developers) developers.add(developer.name)

                        Text(
                            text = developers.joinToString(", "),
                            style = MaterialTheme.typography.caption2,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            Modifier.padding(top = 8.dp)
                        ) {
                            for (license in library.licenses) CompactChip(
                                label = { Text(
                                    license.name,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                ) },
                                onClick = { },
                                colors = ChipDefaults.gradientBackgroundChipColors()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertLegalDetails(
    library: Library?,
    sc: ScaffoldContext<ScalingLazyListState>,
    onClose: () -> Unit
) {
    if (library == null) return

    RotaryScrollAlert(
        scrollState = sc.scrollableState,
        icon = {
            Icon(
                Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = stringResource(id = R.string.legal_title),
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(align = Alignment.Center),
            )
        },
        title = { Text(text = library.name, textAlign = TextAlign.Center) },
        message = {
            Text(
                text = library.description!!,
                style = MaterialTheme.typography.body2
            )
        }
    ) {
        val licenses = library.licenses.toList()
        items(licenses.size) {
            Column {
                val license = licenses[it]
                Text(
                    text = license.name,
                    style = MaterialTheme.typography.title3
                )
                if (license.licenseContent != null) {
                    Text(
                        text = license.licenseContent!!,
                        style = MaterialTheme.typography.caption3
                    )
                }
            }
        }
        item {
            Chip(
                modifier = Modifier.padding(top = 8.dp),
                label = { Text(stringResource(id = R.string.close)) },
                onClick = onClose,
                colors = ChipDefaults.primaryChipColors(),
            )
        }
    }
}
