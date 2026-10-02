package com.mundo.keybowl

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.mundo.keybowl.models.Routes
import com.mundo.keybowl.screen.AuthScreen
import com.mundo.keybowl.screen.HomeScreen
import com.mundo.keybowl.ui.theme.KeyBowlTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeyBowlTheme {
                val backStack = remember {
                    mutableStateListOf<Routes>(
                        Routes.AuthRoute
                    )
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            Routes.AuthRoute -> NavEntry(key) {
                                AuthScreen(
                                    onNavigateToHome = {
                                        backStack.clear()
                                        backStack.add(Routes.HomeRoute)
                                    }
                                )
                            }
                            Routes.HomeRoute -> NavEntry(key) {
                                HomeScreen()
                            }
                        }
                    }
                )
            }
        }
    }
}