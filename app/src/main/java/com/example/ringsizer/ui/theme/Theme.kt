package com.example.ringsizer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Charcoal,
    secondary = GoldMuted,
    onSecondary = Charcoal,
    background = Charcoal,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnDark
)

@Composable
fun RingSizerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}