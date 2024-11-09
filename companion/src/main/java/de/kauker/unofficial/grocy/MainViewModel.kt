package de.kauker.unofficial.grocy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(
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

    @SuppressLint("VisibleForTests")
    fun sendIntent(
        context: Context,
        apiUrl: String,
        apiToken: String
    ) {
        viewModelScope.launch {
            _state.emit(State.Loading)

            val dataClient: DataClient = Wearable.getDataClient(context)
            val putDataReq: PutDataRequest = PutDataMapRequest.create("/auth").run {
                dataMap.putString("url", apiUrl)
                dataMap.putString("token", apiToken)
                asPutDataRequest()
            }

            val putDataTask: Task<DataItem> = dataClient.putDataItem(putDataReq)
            putDataTask.addOnSuccessListener { viewModelScope.launch { _state.emit(State.Success) }}
            putDataTask.addOnFailureListener { viewModelScope.launch { _state.emit(State.Failed) }}
        }
    }

}

