package com.mokostudio.moko.ui.navigation

import android.net.Uri

sealed class MokoDestination(val route: String) {
    data object Home : MokoDestination("home")
    data object Editor : MokoDestination("editor/{imageUri}") {
        const val IMAGE_URI_ARGUMENT = "imageUri"

        fun createRoute(imageUri: Uri): String {
            return "editor/${Uri.encode(imageUri.toString())}"
        }
    }
}
