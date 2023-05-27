package de.kauker.unofficial.grocy.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun WearAppTheme(
    ambientMode: Boolean,
    amoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if(ambientMode) wearColorPaletteAmbient else if(amoledMode) wearColorPaletteAmoled else wearColorPalette,
        typography = Typography,
        content = content
    )
}
