package com.mundo.keybowl.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mundo.keybowl.utils.BiometricHelper
import com.mundo.keybowl.utils.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    var errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, pass: String, onLoginSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Veuillez remplir tous les champs"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                secureStorage.saveCredentials(email, pass)
                onLoginSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erreur d'authentification"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkCredentials(): Boolean {
        val email = secureStorage.getEmail()
        val pass = secureStorage.getPassword()
        return email != null && pass != null
    }

    fun handleBiometry(activity: FragmentActivity?, onNavigateToHome: () -> Unit) {
        if (activity != null) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    // check if login is successful
                    login(secureStorage.getEmail()!!, secureStorage.getPassword()!!, onNavigateToHome)
                },
                onError = { errorMsg ->
                    _errorMessage.value = errorMsg
                }
            )
        } else {
            _errorMessage.value = "Activité introuvable pour afficher l'authentification biométrique"
        }
    }
}