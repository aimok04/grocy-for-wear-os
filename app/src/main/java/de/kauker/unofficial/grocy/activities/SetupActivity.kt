package de.kauker.unofficial.grocy.activities

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.*
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.horologist.compose.focus.rememberActiveFocusRequester
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import de.kauker.unofficial.grocy.INSTALL_COMPANION_APP_URL
import de.kauker.unofficial.grocy.MainActivity
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.theme.WearAppTheme
import java.net.URLDecoder
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.json.JSONObject

class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (intent.dataString != null) {
                val encodedJson = intent.dataString!!.substring(19)
                val json = JSONObject(URLDecoder.decode(encodedJson, "UTF-8"))

                val apiUrl = json.getString("apiUrl")
                val apiToken = json.getString("apiToken")

                setContent {
                    WearAppTheme {
                        SetupConfirmationComp(activity = this, apiUrl, apiToken)
                    }
                }

                return
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        val viewModel = SetupViewModel(application)

        setContent {
            WearAppTheme {
                SetupComp(viewModel)
            }
        }
    }
}

@Composable
fun SetupTitle() {
    Text(
        modifier = Modifier.padding(top = 26.dp),
        text = stringResource(id = R.string.setup_title),
        style = MaterialTheme.typography.title2
            .copy(fontWeight = FontWeight.Bold)
    )
}

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun SetupConfirmationComp(activity: SetupActivity, apiUrl: String, apiToken: String) {
    val focusRequester = rememberActiveFocusRequester()
    val scrollableState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.focusRequester(focusRequester)
            .rotaryWithScroll(focusRequester, scrollableState),
        state = scrollableState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { SetupTitle() }

        item {
            Text(
                text = stringResource(id = R.string.setup_prompt_save_credentials),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        }

        item {
            Text(
                text = apiUrl,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption2
            )
        }

        item {
            Text(
                text = apiToken,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption2
            )
        }

        item {
            Row {
                /* save credentials button */
                Chip(
                    modifier = Modifier.padding(end = 4.dp),
                    label = { Text(stringResource(id = R.string.yes)) },
                    colors = ChipDefaults.gradientBackgroundChipColors(),
                    onClick = {
                        val sp = activity.getSharedPreferences(
                            "credentials",
                            ComponentActivity.MODE_PRIVATE
                        )
                        sp.edit().putString("apiUrl", apiUrl)
                            .putString("apiToken", apiToken).apply()

                        activity.startActivity(Intent(activity, MainActivity().javaClass))
                        activity.finish()
                    }
                )
                /* reject credentials button */
                Chip(
                    modifier = Modifier.padding(start = 4.dp),
                    label = { Text(stringResource(id = R.string.no)) },
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = { activity.finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalHorologistComposeLayoutApi::class)
@Composable
fun SetupComp(setupViewModel: SetupViewModel) {
    val focusRequester = rememberActiveFocusRequester()
    val scrollableState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.focusRequester(focusRequester)
            .rotaryWithScroll(focusRequester, scrollableState),
        state = scrollableState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { SetupTitle() }

        item {
            Text(
                text = stringResource(id = R.string.setup_prompt_open_app),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                Modifier.padding(top = 8.dp)
            ) {
                Chip(
                    modifier = Modifier.padding(end = 4.dp),
                    label = { Text(stringResource(id = R.string.open)) },
                    colors = ChipDefaults.gradientBackgroundChipColors(),
                    onClick = {
                        setupViewModel.openUriRemotely(Uri.parse("gfwo://open"))
                    }
                )
                Chip(
                    modifier = Modifier.padding(start = 4.dp),
                    label = { Text(stringResource(id = R.string.install)) },
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = {
                        setupViewModel.openUriRemotely(Uri.parse(INSTALL_COMPANION_APP_URL))
                    }
                )
            }
        }
    }
}

class SetupViewModel constructor(application: Application) : AndroidViewModel(
    application
) {

    fun openUriRemotely(uri: Uri) {
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            try {
                RemoteActivityHelper(context).startRemoteActivity(
                    Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setData(uri)
                ).await()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

}
