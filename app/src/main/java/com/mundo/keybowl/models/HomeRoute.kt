package com.mundo.keybowl.models

import androidx.compose.runtime.Composable
import com.mundo.keybowl.screen.HomeScreen
import com.mundo.keybowl.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel = koinViewModel()
) {
    HomeScreen()
}