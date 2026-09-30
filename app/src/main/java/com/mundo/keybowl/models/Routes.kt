package com.mundo.keybowl.models

sealed interface Routes {
    data object AuthRoute: Routes
    data object HomeRoute: Routes
}