package de.kauker.unofficial.grocy.models

import androidx.compose.runtime.Composable
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import com.google.android.horologist.compose.navscaffold.ScaffoldContext

class Route(
    val route: String,
    val comp: @Composable (context: ScaffoldContext<ScalingLazyListState>) -> Unit
)
