@file:OptIn(ExperimentalMaterial3Api::class)

package de.kauker.unofficial.grocy.phone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.kauker.unofficial.grocy.phone.activities.LegalActivity
import de.kauker.unofficial.grocy.phone.ui.theme.GrocyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val viewModel = MainViewModel(application)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    setContent {
                        GrocyTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                MainScaffold(state, viewModel, this@MainActivity)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(state: MainViewModel.State, viewModel: MainViewModel, context: Context) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var apiUrl by rememberSaveable { mutableStateOf("") }
    var apiToken by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, LegalActivity().javaClass))
                    }) {
                        Icon(
                            Icons.Rounded.MenuBook,
                            stringResource(id = R.string.legal_title)
                        )
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = it.calculateBottomPadding()
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state is MainViewModel.State.Default) {
                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        value = apiUrl,
                        leadingIcon = { Icon(Icons.Rounded.Public, "Url") },
                        label = { Text(stringResource(id = R.string.main_field_api_url)) },
                        onValueChange = { text -> apiUrl = text }
                    )

                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.sendIntent(apiUrl, apiToken)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        value = apiToken,
                        leadingIcon = { Icon(Icons.Rounded.Key, "Token") },
                        label = { Text(stringResource(id = R.string.main_field_api_token)) },
                        onValueChange = { text -> apiToken = text }
                    )

                    Button(
                        modifier = Modifier.padding(top = 16.dp),
                        content = { Text(stringResource(id = R.string.main_button_send)) },
                        onClick = {
                            viewModel.sendIntent(apiUrl, apiToken)
                        }
                    )
                } else if (state is MainViewModel.State.Loading) {
                    CircularProgressIndicator()
                } else {
                    val emoji = if (state is MainViewModel.State.Success) "⌚" else "\uD83E\uDD14"
                    val message =
                        if (state is MainViewModel.State.Success) stringResource(id = R.string.main_prompt_sent) else stringResource(
                            id = R.string.main_prompt_failed
                        )

                    Row(
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                    ) {
                        Text(
                            emoji,
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            message,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}
