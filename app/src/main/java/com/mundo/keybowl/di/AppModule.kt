package com.mundo.keybowl.di

import com.google.firebase.auth.FirebaseAuth
import com.mundo.keybowl.utils.SecureStorage
import com.mundo.keybowl.viewmodel.AuthViewModel
import com.mundo.keybowl.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { SecureStorage(get()) }

    viewModel { AuthViewModel(get(), get()) }
    viewModel { HomeViewModel() }
}