package com.mundo.keybowl.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mundo.keybowl.screen.AuthScreen
import com.mundo.keybowl.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthRoute(
    onNavigateToHome: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    AuthScreen(
        onLoginClick = { email, password ->
            authViewModel.login(email, password, onNavigateToHome)
        },
        isLoading = isLoading,
        errorMessage = errorMessage
    )
}