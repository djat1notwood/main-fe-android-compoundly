package com.djatslabs.compoundly

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.djatslabs.compoundly.data.SharedPreferencesOnboardingPreferences
import com.djatslabs.compoundly.ui.screens.CompoundlyApp
import com.djatslabs.compoundly.ui.state.CompoundlyViewModel
import com.djatslabs.compoundly.ui.state.CompoundlyViewModelFactory
import com.djatslabs.compoundly.ui.theme.CompoundlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        val onboardingPreferences = SharedPreferencesOnboardingPreferences(applicationContext)
        setContent {
            CompoundlyTheme {
                val viewModel: CompoundlyViewModel = viewModel(
                    factory = CompoundlyViewModelFactory(onboardingPreferences)
                )
                CompoundlyApp(viewModel = viewModel)
            }
        }
    }
}
