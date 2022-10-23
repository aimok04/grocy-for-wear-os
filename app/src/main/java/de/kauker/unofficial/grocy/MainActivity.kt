package de.kauker.unofficial.grocy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import de.kauker.unofficial.grocy.activities.SettingsActivity
import de.kauker.unofficial.grocy.activities.SetupActivity
import de.kauker.unofficial.grocy.models.ShoppingListGrocyItemEntry
import de.kauker.unofficial.grocy.models.ShoppingListTitleEntry
import de.kauker.unofficial.grocy.theme.WearAppTheme
import de.kauker.unofficial.grocy.ui.ScalingLazyColumnWithRSB
import kotlinx.coroutines.launch

const val ACTION_SIGN_OUT = 0
const val ACTION_SHOW_SETTINGS = 1

class MainActivity : ComponentActivity() {

    private var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        if (!sp.contains("apiUrl") || !sp.contains("apiToken")) {
            startActivity(Intent(this, SetupActivity().javaClass))
            finish()
            return
        }

        this.viewModel =
            MainViewModel(sp.getString("apiUrl", "")!!, sp.getString("apiToken", "")!!, application)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel!!.state.collect { state ->
                    setContent {

                        WearApp(
                            state,
                            viewModel!!
                        ) {
                            if (it == ACTION_SIGN_OUT) {
                                getSharedPreferences("credentials", MODE_PRIVATE)
                                    .edit().remove("apiUrl").remove("apiToken").apply()
                                startActivity(Intent(this@MainActivity, SetupActivity().javaClass))
                                finish()
                            } else if (it == ACTION_SHOW_SETTINGS) {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        SettingsActivity().javaClass
                                    )
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onResume() {
        viewModel!!.load()
        super.onResume()
    }
}

@Composable
fun WearApp(
    state: MainViewModel.State,
    viewModel: MainViewModel,
    triggerAction: (action: Int) -> Unit
) {
    WearAppTheme {
        Scaffold(timeText = {
            TimeText()
        }) {
            var showSignOutAlert by remember { mutableStateOf(false) }
            Dialog(showDialog = showSignOutAlert, onDismissRequest = { showSignOutAlert = false }) {
                AlertSignOut(onClickPrimary = {
                    showSignOutAlert = false
                    triggerAction(ACTION_SIGN_OUT)
                }, onClickSecondary = {
                    showSignOutAlert = false
                })
            }

            if (state is MainViewModel.State.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ScalingLazyColumnWithRSB {
                    item {
                        /* page title */
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 26.dp),
                            text = stringResource(id = R.string.main_title),
                            style = MaterialTheme.typography.title2
                                .copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (state is MainViewModel.State.Data) {
                        items(state.data!!.shoppingListItems.size) {
                            val item = state.data.shoppingListItems[it]

                            if (item is ShoppingListTitleEntry) {
                                /*  product category titles */
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    text = if (item.title != null) item.title!! else stringResource(
                                        id = item.titleId!!
                                    ),
                                    style = MaterialTheme.typography.body2,
                                    textAlign = TextAlign.Center
                                )
                            } else if (item is ShoppingListGrocyItemEntry) {
                                /* actual shopping list items */
                                ShoppingListEntryCard(item = item, viewModel = viewModel)
                            }
                        }
                    } else if (state is MainViewModel.State.ConnectionIssue) {
                        item {
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp),
                                text = stringResource(id = R.string.main_prompt_connection_issues),
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    item {
                        /* sign out and licenses buttons */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                modifier = Modifier.padding(end = 4.dp),
                                onClick = { showSignOutAlert = true },
                                colors = ButtonDefaults.secondaryButtonColors()
                            ) {
                                Icon(
                                    Icons.Rounded.Logout,
                                    contentDescription = stringResource(id = R.string.sign_out)
                                )
                            }

                            Chip(
                                modifier = Modifier.padding(start = 4.dp),
                                colors = ChipDefaults.gradientBackgroundChipColors(),
                                label = { Text(stringResource(id = R.string.settings)) },
                                onClick = { triggerAction(ACTION_SHOW_SETTINGS) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListEntryCard(item: ShoppingListGrocyItemEntry, viewModel: MainViewModel) {
    val entry = item.entry
    val alpha = if (entry.done) 0.5f else 1f

    TitleCard(
        title = { Text(entry.product!!.name) },
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            .alpha(alpha),
        onClick = {
            viewModel.toggleShoppingListEntryDoneStatus(entry)
        }
    ) {
        val quantityUnit =
            if (entry.quantityUnit == null) "" else if (entry.amount == "1") entry.quantityUnit?.name else entry.quantityUnit?.namePlural
        Text(entry.amount + " " + quantityUnit)
    }
}

@Composable
fun AlertSignOut(onClickPrimary: () -> Unit, onClickSecondary: () -> Unit) {
    Alert(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = {
            Icon(
                Icons.Rounded.Logout,
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
                text = stringResource(id = R.string.main_dialog_logout_message),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
        },
    ) {
        item {
            Row {
                Chip(
                    modifier = Modifier.padding(end = 8.dp),
                    label = { Text(stringResource(id = R.string.yes)) },
                    onClick = onClickPrimary,
                    colors = ChipDefaults.gradientBackgroundChipColors(),
                )
                Chip(
                    modifier = Modifier.padding(start = 8.dp),
                    label = { Text(stringResource(id = R.string.no)) },
                    onClick = onClickSecondary,
                    colors = ChipDefaults.secondaryChipColors(),
                )
            }
        }
    }
}
