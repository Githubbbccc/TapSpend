package com.tapspend.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TapSpendDarkColors = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF04240F),
    background = Color(0xFF080D1A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outlineVariant = Color(0xFF334155)
)

private val TapSpendLightColors = lightColorScheme(
    primary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EDF5),
    onSurfaceVariant = Color(0xFF5A6B80)
)

@Composable
fun TapSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) TapSpendDarkColors else TapSpendLightColors,
        content = content
    )
}
