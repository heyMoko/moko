package com.mokostudio.moko.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mokostudio.moko.ui.editor.EditorScreen
import com.mokostudio.moko.ui.home.HomeScreen

@Composable
fun MokoNavHost(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MokoDestination.Home.route,
        modifier = modifier.padding(contentPadding)
    ) {
        composable(MokoDestination.Home.route) {
            HomeScreen(
                onSelectPhotoClick = {
                    navController.navigate(MokoDestination.Editor.route)
                }
            )
        }

        composable(MokoDestination.Editor.route) {
            EditorScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
