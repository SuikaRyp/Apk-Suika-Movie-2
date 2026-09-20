package com.suikamovie.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SuikaDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextMain,
    primaryContainer = PrimaryBlueSubtle,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = PrimaryBlueLight,
    background = BgBody,
    onBackground = TextMain,
    surface = BgCard,
    onSurface = TextMain,
    surfaceVariant = BgSurface,
    onSurfaceVariant = TextSub,
    outline = BorderColor,
    error = DangerRed,
)

/**
 * Tema utama SuikaMovie - selalu dark mode (app-nya emang didesain gelap
 * dari awal, sama kayak versi web/WebView dulu).
 */
@Composable
fun SuikaMovieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SuikaDarkColorScheme,
        typography = SuikaTypography,
        content = content
    )
}
