package com.planszowsky.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.planszowsky.android.ui.screens.CollectionScreen
import com.planszowsky.android.ui.screens.DetailsScreen
import com.planszowsky.android.ui.screens.SearchScreen

@Composable
fun PlanszowskyNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "collection") {
        composable("collection") {
            CollectionScreen(
                onAddGameClick = { navController.navigate("search") },
                onGameClick = { gameId -> navController.navigate("details/$gameId") }
            )
        }
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "details/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) {
            DetailsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}