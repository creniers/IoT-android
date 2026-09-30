package com.mundo.keybowl.models

sealed interface Routes {
    data object AuthRoute: Routes
}