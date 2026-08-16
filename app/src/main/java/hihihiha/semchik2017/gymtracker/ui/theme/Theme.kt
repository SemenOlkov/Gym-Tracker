package hihihiha.semchik2017.gymtracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GymLime,
    onPrimary = GymBlack,
    primaryContainer = GymLime.copy(alpha = 0.2f),
    onPrimaryContainer = GymLime,
    secondary = GymBlue,
    onSecondary = Color.White,
    background = GymBlack,
    onBackground = GymOnSurface,
    surface = GymDarkGrey,
    onSurface = GymOnSurface,
    surfaceVariant = GymSurface,
    onSurfaceVariant = GymOnSurface,
    error = GymError,
    onError = Color.White,
    tertiary = GymAccent
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF558B2F), // Darker green for light theme legibility
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color.Black,
    secondary = GymBlue
)

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // For gym app, we prefer our custom aggressive theme over dynamic ones
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
