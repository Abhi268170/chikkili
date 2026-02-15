package com.example.helloandroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ── Material color schemes ────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = LightFinanceColors.darkNavy,
    onPrimary = Color.White,
    secondary = LightFinanceColors.dockTextSelected,
    onSecondary = Color.White,
    tertiary = LightFinanceColors.incomeGreen,
    background = LightFinanceColors.screenBg,
    onBackground = LightFinanceColors.darkNavy,
    surface = LightFinanceColors.cardBg,
    onSurface = LightFinanceColors.darkNavy,
    surfaceVariant = Color(0xFFEEF0F5),
    onSurfaceVariant = LightFinanceColors.subtitleGray,
    outline = LightFinanceColors.dividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkFinanceColors.darkNavy,
    onPrimary = DarkFinanceColors.onPrimaryButton,
    secondary = DarkFinanceColors.dockTextSelected,
    onSecondary = Color(0xFF1E1E2A),
    tertiary = DarkFinanceColors.incomeGreen,
    background = DarkFinanceColors.screenBg,
    onBackground = DarkFinanceColors.darkNavy,
    surface = DarkFinanceColors.cardBg,
    onSurface = DarkFinanceColors.darkNavy,
    surfaceVariant = Color(0xFF2A2A3D),
    onSurfaceVariant = DarkFinanceColors.subtitleGray,
    outline = DarkFinanceColors.dividerColor
)

// ══════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ══════════════════════════════════════════════════════════

@Composable
fun HelloAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val financeColors = if (darkTheme) DarkFinanceColors else LightFinanceColors
    val colorScheme   = if (darkTheme) DarkColorScheme    else LightColorScheme

    CompositionLocalProvider(LocalFinanceColors provides financeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ══════════════════════════════════════════════════════════
// CONVENIENCE ACCESSOR
// ══════════════════════════════════════════════════════════

/**
 * Provides typed access to the current [FinanceColors]:
 *
 * ```kotlin
 * val bg = FinanceTheme.colors.screenBg
 * ```
 */
object FinanceTheme {
    val colors: FinanceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinanceColors.current
}