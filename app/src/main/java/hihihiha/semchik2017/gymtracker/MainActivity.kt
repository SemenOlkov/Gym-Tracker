package hihihiha.semchik2017.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import hihihiha.semchik2017.gymtracker.domain.repository.SettingsRepository
import hihihiha.semchik2017.gymtracker.ui.MainScaffold
import hihihiha.semchik2017.gymtracker.ui.theme.GymTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")
            val dynamicColors by settingsRepository.dynamicColors.collectAsState(initial = true)
            
            val useDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            GymTrackerTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColors
            ) {
                MainScaffold()
            }
        }
    }
}
