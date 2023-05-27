package de.kauker.unofficial.grocy

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavGraphBuilder
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.navscaffold.ScaffoldContext
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import com.google.android.horologist.compose.navscaffold.scalingLazyColumnComposable
import de.kauker.unofficial.grocy.activities.SetupActivity
import de.kauker.unofficial.grocy.models.Route
import de.kauker.unofficial.grocy.routes.AddProductRoute
import de.kauker.unofficial.grocy.routes.HomeRoute
import de.kauker.unofficial.grocy.routes.SelectListRoute
import de.kauker.unofficial.grocy.routes.SettingsRoute
import de.kauker.unofficial.grocy.routes.alerts.AlertWelcomeRoute
import de.kauker.unofficial.grocy.routes.delete.DeleteDoneRoute
import de.kauker.unofficial.grocy.routes.settings.SettingsLegalRoute
import de.kauker.unofficial.grocy.routes.settings.SettingsProductGroupsOrderRoute
import de.kauker.unofficial.grocy.theme.WearAppTheme

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    var viewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ambientController = AmbientModeSupport.attach(this)

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        if (!sp.contains("apiUrl") || !sp.contains("apiToken")) {
            startActivity(Intent(this, SetupActivity().javaClass))
            finish()
            return
        }

        this.viewModel =
            MainViewModel(sp.getString("apiUrl", "")!!, sp.getString("apiToken", "")!!, application)

        setContent {
            WearApp(
                viewModel!!
            )
        }
    }


    private lateinit var ambientController: AmbientModeSupport.AmbientController
    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback(this)

    class MyAmbientCallback(
        private val mainActivity: MainActivity
    ) : AmbientModeSupport.AmbientCallback() {

        override fun onEnterAmbient(ambientDetails: Bundle?) {
            mainActivity.viewModel?.ambientMode = true
        }

        override fun onExitAmbient() {
            mainActivity.viewModel?.ambientMode = false
        }

        override fun onUpdateAmbient() {}
    }

}

@Composable
fun WearApp(
    vm: MainViewModel
) {
    val navController = rememberSwipeDismissableNavController()
    vm.rootNavController = navController

    WearAppTheme(
        vm.ambientMode,
        vm.amoledMode
    ) {
        Scaffold(
            Modifier.background(MaterialTheme.colors.background)
        ) {
            fun NavGraphBuilder.defaultListComposable(route: String, comp: @Composable (context: ScaffoldContext<ScalingLazyListState>) -> Unit) {
                @Suppress("DEPRECATION")
                scalingLazyColumnComposable(route, scrollStateBuilder = { ScalingLazyListState() }) {
                    comp(it)
                }
            }

            val routes = listOf(
                Route("home") { HomeRoute(mainVM = vm, sc = it) },

                Route("add") { AddProductRoute(vm = vm, sc = it) },
                Route("selectList") { SelectListRoute(mainVM = vm, sc = it) },

                Route("delete/done") { DeleteDoneRoute(vm = vm, sc = it) },

                Route("settings") { SettingsRoute(vm = vm, sc = it) },
                Route("settings/productGroupsOrder") { SettingsProductGroupsOrderRoute(mainVM = vm, sc = it) },
                Route("settings/legal") { SettingsLegalRoute(sc = it) },

                Route("alerts/welcome") { AlertWelcomeRoute(vm = vm, sc = it) }
            )

            WearNavScaffold(
                navController = navController,
                startDestination = "home"
            ) {
                routes.forEach { route ->
                    defaultListComposable(route.route, route.comp)
                }
            }
        }
    }
}
