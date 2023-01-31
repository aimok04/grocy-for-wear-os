package de.kauker.unofficial.grocy.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.kauker.unofficial.grocy.R
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import de.kauker.unofficial.grocy.theme.GrocyTheme

class LegalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GrocyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LegalScaffold(this)
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScaffold(activity: LegalActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.legal_title)) },
                navigationIcon = {
                    IconButton(onClick = { activity.onBackPressed() }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(id = R.string.action_back))
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding(),
                    bottom = it.calculateBottomPadding()
                ),
            contentAlignment = Alignment.Center
        ) {
            LibrariesContainer(
                Modifier.fillMaxSize()
            )
        }
    }
}
