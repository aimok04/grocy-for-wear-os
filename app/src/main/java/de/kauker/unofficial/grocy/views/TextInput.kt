package de.kauker.unofficial.grocy.views

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.wear.input.RemoteInputIntentHelper

@Composable
fun TextInput(
    label: String,
    onTextReceived: (data: CharSequence) -> Unit,
    onDismiss: () -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == 0) {
                onDismiss()
                return@rememberLauncherForActivityResult
            }

            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)!!
                onTextReceived(results.getCharSequence("data")!!)
            }
        }

    LaunchedEffect(Unit) {
        val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInputs: List<RemoteInput> = listOf(
            RemoteInput.Builder("data")
                .setLabel(label)
                .build()
        )

        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        launcher.launch(intent)
    }
}
