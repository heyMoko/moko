package com.mokostudio.moko.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MokoWhite,
    onPrimary = MokoBlack,
    background = MokoBlack,
    onBackground = MokoWhite,
    surface = MokoBlack,
    onSurface = MokoWhite,
    surfaceVariant = MokoDarkSurface,
    onSurfaceVariant = MokoGray
)

private val LightColorScheme = lightColorScheme(
    primary = MokoBlack,
    onPrimary = MokoWhite,
    background = MokoWhite,
    onBackground = MokoBlack,
    surface = MokoWhite,
    onSurface = MokoBlack,
    surfaceVariant = MokoLightGray,
    onSurfaceVariant = MokoGray
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
