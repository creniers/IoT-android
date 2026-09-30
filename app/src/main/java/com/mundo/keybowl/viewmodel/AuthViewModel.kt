package com.mundo.keybowl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
                onLoginSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Erreur d'authentification"
            } finally {
                _isLoading.value = false
            }
        }
    }
}