package de.kauker.unofficial.grocy

import android.app.Application
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.CATEGORY_BROWSABLE
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainViewModel constructor(application: Application) : AndroidViewModel(
    application
) {

    sealed class State {
        object Default : State()
        object Loading : State()

        object Success : State()
        object Failed : State()
    }

    private var _state = MutableStateFlow<State>(State.Default)
    val state = _state.asStateFlow()

    fun sendIntent(apiUrl: String, apiToken: String) {
        viewModelScope.launch {
            _state.emit(State.Loading)

            try {
                val json = JSONObject().put("apiUrl", apiUrl).put("apiToken", apiToken)

                RemoteActivityHelper(getApplication()).startRemoteActivity(
                    Intent(ACTION_VIEW)
                        .addCategory(CATEGORY_BROWSABLE)
                        .setData(
                            Uri.parse(
                                "gfwo://setup/?data=" + withContext(Dispatchers.IO) {
                                    URLEncoder.encode(
                                        json.toString(),
                                        "UTF-8"
                                    )
                                }
                            )
                        )
                ).await()

                _state.emit(State.Success)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                _state.emit(State.Failed)
            }
        }
    }

}

