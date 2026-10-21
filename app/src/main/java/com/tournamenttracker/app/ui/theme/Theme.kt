package com.tournamenttracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = Gold80,
    tertiary = GreenGrey80,
    background = NearBlack,
    surface = NearBlack,
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = Gold40,
    tertiary = GreenGrey40,
)

@Composable
fun TournamentTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
