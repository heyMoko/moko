package com.mokostudio.moko.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
                onPhotoSelected = { photoUri ->
                    navController.navigate(MokoDestination.Editor.createRoute(photoUri))
                }
            )
        }

        composable(
            route = MokoDestination.Editor.route,
            arguments = listOf(
                navArgument(MokoDestination.Editor.IMAGE_URI_ARGUMENT) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments
                ?.getString(MokoDestination.Editor.IMAGE_URI_ARGUMENT)
                ?.let(android.net.Uri::decode)

            EditorScreen(
                imageUri = imageUri,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
