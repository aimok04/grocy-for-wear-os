package de.kauker.unofficial.grocy

import android.app.Application
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavHostController
import de.kauker.unofficial.grocy.routes.HomeViewModel
import de.kauker.unofficial.grocy.routes.settings.SettingsProductGroupsOrderViewModel
import de.kauker.unofficial.sdk.grocy.GrocyClient

class MainViewModel constructor(apiUrl: String, apiToken: String, application: Application) :
    AndroidViewModel(
        application
    ) {

    /* ambient mode */
    var ambientMode by mutableStateOf(false)

    /* nav */
    var rootNavController: NavHostController? = null

    /* vms */
    val vmHomeRoute = HomeViewModel(this, getApplication())
    val vmSettingsProductGroupsOrderRoute = SettingsProductGroupsOrderViewModel(this, getApplication())

    var settingsSp: SharedPreferences = getApplication<Application>().getSharedPreferences(
        "settings",
        ComponentActivity.MODE_PRIVATE
    )

    var grocyClient = GrocyClient(application, apiUrl, apiToken)

}
