package de.kauker.unofficial.grocy.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.theme.WearAppTheme
import de.kauker.unofficial.grocy.ui.ScalingLazyColumnWithRSB

class LegalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val libs = Libs.Builder().withContext(this).build()

        setContent {
            WearAppTheme {
                LibrariesList(libraries = libs.libraries)
            }
        }
    }
}

@Composable
fun LibrariesList(libraries: List<Library>) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedLibrary: Library? by remember { mutableStateOf(null) }
    Dialog(showDialog = showDetailsDialog, onDismissRequest = { showDetailsDialog = false }) {
        AlertLegalDetails(selectedLibrary) { showDetailsDialog = false }
    }

    ScalingLazyColumnWithRSB {
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

        items(libraries.size) {
            val library = libraries[it]

            TitleCard(
                title = { Text(library.name + " @ " + library.artifactVersion) },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .background(MaterialTheme.colors.background, RoundedCornerShape(24.dp)),
                onClick = {
                    selectedLibrary = library
                    showDetailsDialog = true
                },
                backgroundPainter = painterResource(id = R.drawable.empty_background)
            ) {
                val developers = ArrayList<String?>()
                for (developer in library.developers) developers.add(developer.name)

                Text(developers.joinToString(", "))

                Row {
                    for (license in library.licenses) CompactChip(
                        label = { Text(license.name) },
                        onClick = { },
                        colors = ChipDefaults.gradientBackgroundChipColors()
                    )
                }
            }

        }
    }
}

@Composable
fun AlertLegalDetails(library: Library?, onClose: () -> Unit) {
    if (library == null) return

    Alert(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = {
            Icon(
                Icons.Rounded.MenuBook,
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
        },
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
