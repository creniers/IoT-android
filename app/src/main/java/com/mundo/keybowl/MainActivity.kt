package com.mundo.keybowl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.mundo.keybowl.models.Routes
import com.mundo.keybowl.route.AuthRoute
import com.mundo.keybowl.ui.theme.KeyBowlTheme

class MainActivity : ComponentActivity() {
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
                                AuthRoute()
                            }
                        }
                    }
                )
            }
        }
    }
}