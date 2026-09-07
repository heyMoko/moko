package com.mokostudio.moko.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MokoTangerine,
    onPrimary = MokoDeepBerry,
    secondary = MokoCoral,
    background = MokoInk,
    onBackground = MokoCream,
    surface = MokoDarkSurface,
    onSurface = MokoCream,
    surfaceVariant = MokoDarkSurface,
    onSurfaceVariant = MokoPeach
)

private val LightColorScheme = lightColorScheme(
    primary = MokoBerry,
    onPrimary = MokoCream,
    secondary = MokoTangerine,
    background = MokoCream,
    onBackground = MokoInk,
    surface = Color.White,
    onSurface = MokoInk,
    surfaceVariant = MokoBlush,
    onSurfaceVariant = MokoMutedBrown
)

@Composable
fun MokoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
