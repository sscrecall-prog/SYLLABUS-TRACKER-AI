package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.SyllabusTrackerTheme
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.OnboardingPreferences

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val appSettings by settingsViewModel.appSettings.collectAsState()
            var isOnboardingCompleted by remember {
                mutableStateOf(OnboardingPreferences.isOnboardingCompleted(context))
            }

            SyllabusTrackerTheme(themeMode = appSettings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = isOnboardingCompleted,
                        animationSpec = tween(durationMillis = 400),
                        label = "onboarding_crossfade"
                    ) { completed ->
                        if (!completed) {
                            OnboardingScreen(
                                onGetStarted = {
                                    OnboardingPreferences.setOnboardingCompleted(context, true)
                                    isOnboardingCompleted = true
                                }
                            )
                        } else {
                            MainScreen(
                                onReplayOnboarding = {
                                    isOnboardingCompleted = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

