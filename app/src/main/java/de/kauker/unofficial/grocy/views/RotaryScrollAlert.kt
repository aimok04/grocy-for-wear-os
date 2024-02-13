package de.kauker.unofficial.grocy.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun RotaryScrollAlert(
    modifier: Modifier = Modifier,
    title: @Composable (ColumnScope.() -> Unit),
    icon: @Composable (ColumnScope.() -> Unit),
    message: @Composable (ColumnScope.() -> Unit),
    scrollState: ScalingLazyListState,
    content: (ScalingLazyListScope.() -> Unit)
) {
    val focusRequester = rememberActiveFocusRequester()

    Alert(
        modifier = modifier
            .focusRequester(focusRequester)
            .rotaryWithScroll(focusRequester, scrollState),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        scrollState = scrollState,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
        icon = icon,
        iconColor = MaterialTheme.colors.primary,
        title = title,
        message = message,
        content = content
    )
}
