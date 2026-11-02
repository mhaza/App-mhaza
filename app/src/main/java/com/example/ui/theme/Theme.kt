package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HamdanLightColorScheme = lightColorScheme(
    primary = HamdanPrimary,
    onPrimary = HamdanOnPrimary,
    primaryContainer = HamdanPrimaryContainer,
    onPrimaryContainer = HamdanOnPrimaryContainer,
    secondary = HamdanSecondary,
    onSecondary = HamdanOnSecondary,
    secondaryContainer = HamdanSecondaryContainer,
    onSecondaryContainer = HamdanOnSecondaryContainer,
    background = HamdanBackground,
    onBackground = HamdanOnBackground,
    surface = HamdanSurface,
    onSurface = HamdanOnSurface,
    surfaceVariant = HamdanSurfaceVariant,
    onSurfaceVariant = HamdanOnSurfaceVariant,
    outline = HamdanOutline,
    outlineVariant = HamdanOutlineVariant
)

private val HamdanDarkColorScheme = darkColorScheme(
    primary = HamdanPrimaryDark,
    onPrimary = HamdanOnPrimaryDark,
    primaryContainer = HamdanPrimaryContainerDark,
    onPrimaryContainer = HamdanOnPrimaryContainerDark,
    secondary = HamdanSecondaryDark,
    onSecondary = HamdanOnSecondaryDark,
    secondaryContainer = HamdanSecondaryContainerDark,
    onSecondaryContainer = HamdanOnSecondaryContainerDark,
    background = HamdanBackgroundDark,
    onBackground = HamdanOnBackgroundDark,
    surface = HamdanSurfaceDark,
    onSurface = HamdanOnSurfaceDark,
    surfaceVariant = HamdanSurfaceVariantDark,
    onSurfaceVariant = HamdanOnSurfaceVariantDark,
    outline = HamdanOutline,
    outlineVariant = HamdanOutlineVariant
)

@Composable
fun HamdanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HamdanDarkColorScheme else HamdanLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
