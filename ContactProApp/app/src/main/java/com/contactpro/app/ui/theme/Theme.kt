package com.contactpro.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = HamsaaPrimary,
    onPrimary        = Color.White,
    primaryContainer = HamsaaAccent,
    onPrimaryContainer = HamsaaPrimary,
    secondary        = HamsaaSecondary,
    onSecondary      = Color.White,
    background       = LightBg,
    onBackground     = TextPrimary,
    surface          = LightSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = LightBorder.copy(alpha = 0.5f),
    onSurfaceVariant = TextSecondary,
    outline          = LightBorder,
    error            = Error,
    onError          = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary          = HamsaaSecondary, // Indigo pops better on dark
    onPrimary        = Color.White,
    primaryContainer = HamsaaSecondary.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White,
    background       = DarkBg,
    onBackground     = DarkTextPrimary,
    surface          = DarkSurface,
    onSurface        = DarkTextPrimary,
    surfaceVariant   = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline          = DarkBorder,
    error            = Error,
    onError          = Color.White
)

@Composable
fun HamsaaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // User requested professional light color, so we force light theme if preferred
    // but typically we should respect system settings unless specified otherwise.
    // The user said "change it to any professional light color", implying they want light mode.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
