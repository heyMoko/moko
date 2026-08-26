package com.mokostudio.moko.ui.navigation

sealed class MokoDestination(val route: String) {
    data object Home : MokoDestination("home")
    data object Editor : MokoDestination("editor")
}
