package de.kauker.unofficial.grocy

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import de.kauker.unofficial.GROCY_SUPPORTED_VERSIONS
import de.kauker.unofficial.grocy.routes.HomeViewModel
import de.kauker.unofficial.grocy.routes.settings.SettingsProductGroupsOrderViewModel
import de.kauker.unofficial.sdk.grocy.GrocyClient
import de.kauker.unofficial.sdk.grocy.models.GrocySystemInfo
import de.kauker.unofficial.sdk.grocy.models.retrieveSystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(apiUrl: String, apiToken: String, application: Application) :
    AndroidViewModel(
        application
    ) {

    /* ui modes */
    var amoledMode by mutableStateOf(false)
    var ambientMode by mutableStateOf(false)

    var paused by mutableStateOf(false)

    /* nav */
    var rootNavController: NavHostController? = null

    /* vms */
    val vmHomeRoute = HomeViewModel(this, getApplication())
    val vmSettingsProductGroupsOrderRoute = SettingsProductGroupsOrderViewModel(this, getApplication())

    var settingsSp: SharedPreferences = getApplication<Application>().getSharedPreferences(
        "settings",
        Context.MODE_PRIVATE
    )

    var grocyClient = GrocyClient(application, apiUrl, apiToken)
    var grocySystemInfo: GrocySystemInfo? = null

    init {
        amoledMode = settingsSp.getBoolean("amoledMode", true)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    grocySystemInfo = grocyClient.retrieveSystemInfo()
                    if(GROCY_SUPPORTED_VERSIONS.contains(grocySystemInfo?.grocyVersion?.version?: "")) return@withContext

                    /* do not open alert twice */
                    if(settingsSp.getString("latestUnsupportedVersion", "") == grocySystemInfo?.grocyVersion?.version) return@withContext

                    withContext(Dispatchers.Main) {
                        delay(500)
                        rootNavController?.navigate("alerts/unsupported")
                    }
                }catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}
