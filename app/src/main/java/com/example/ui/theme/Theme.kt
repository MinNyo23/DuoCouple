package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricLavender,
    secondary = CosmicCyan,
    tertiary = SweetheartedPeach,
    background = ObsidianBackground,
    surface = GlassSurfaceDark,
    onBackground = GlassTextPrimary,
    onSurface = GlassTextPrimary,
    surfaceVariant = Color(0xFFEBE6FC),
    onSurfaceVariant = SecondaryTextLavender
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricLavender,
    secondary = CosmicCyan,
    tertiary = SweetheartedPeach,
    background = ObsidianBackground,
    surface = GlassSurfaceDark,
    onBackground = GlassTextPrimary,
    onSurface = GlassTextPrimary,
    surfaceVariant = Color(0xFFEBE6FC),
    onSurfaceVariant = SecondaryTextLavender
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Use light glass theme by default!
    dynamicColor: Boolean = false, // Set to false to preserve our custom premium palette values
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
