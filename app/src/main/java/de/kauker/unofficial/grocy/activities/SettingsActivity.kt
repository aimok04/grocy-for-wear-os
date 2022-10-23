package de.kauker.unofficial.grocy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import de.kauker.unofficial.grocy.R
import de.kauker.unofficial.grocy.theme.WearAppTheme
import de.kauker.unofficial.grocy.ui.ScalingLazyColumnWithRSB

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearAppTheme {
                SettingsComp(context = this)
            }
        }
    }
}

@Composable
fun SettingsComp(context: Context) {
    ScalingLazyColumnWithRSB(
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
                label = { Text(stringResource(id = R.string.settings_button_product_groups_order)) },
                colors = ChipDefaults.gradientBackgroundChipColors(),
                onClick = {
                    context.startActivity(Intent(context, ProductGroupsOrderActivity().javaClass))
                }
            )
        }

        item {
            Chip(
                modifier = Modifier.padding(top = 4.dp),
                label = { Text(stringResource(id = R.string.settings_button_legal_stuff)) },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = {
                    context.startActivity(Intent(context, LegalActivity().javaClass))
                }
            )
        }
    }
}
