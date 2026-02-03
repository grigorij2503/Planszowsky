package com.planszowsky.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.planszowsky.android.ui.screens.CollectionScreen
import com.planszowsky.android.ui.screens.DetailsScreen
import com.planszowsky.android.ui.screens.DiceScreen
import com.planszowsky.android.ui.screens.ProfileScreen
import com.planszowsky.android.ui.screens.RandomizerScreen
import com.planszowsky.android.ui.screens.ScanScreen
import com.planszowsky.android.ui.screens.SearchScreen
import com.planszowsky.android.ui.screens.WishlistScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Collection : Screen("collection", "Kolekcja", Icons.Default.Home)
    object DiceRoller : Screen("dice_roller", "Kostki", Icons.Default.Casino)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite)
    object Profile : Screen("profile", "Profil", Icons.Default.AccountCircle)
}

@Composable
fun PlanszowskyMainContainer() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Collection.route,
        Screen.DiceRoller.route,
        Screen.Wishlist.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(Screen.Collection, Screen.DiceRoller, Screen.Wishlist, Screen.Profile)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Collection.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Collection.route) {
                CollectionScreen(
                    onAddGameClick = { navController.navigate("search") },
                    onGameClick = { gameId -> navController.navigate("details/$gameId") },
                    onRandomizerClick = { navController.navigate("randomizer") },
                    onScanClick = { navController.navigate("scan") }
                )
            }
            composable(Screen.DiceRoller.route) {
                DiceScreen()
            }
            composable(Screen.Wishlist.route) { 
                WishlistScreen(
                    onGameClick = { gameId -> navController.navigate("details/$gameId") }
                )
            }
            composable(Screen.Profile.route) { ProfileScreen() }
            
            composable(
                route = "search?query={query}",
                arguments = listOf(navArgument("query") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val initialQuery = backStackEntry.arguments?.getString("query")
                SearchScreen(
                    initialQuery = initialQuery,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("scan") {
                ScanScreen(
                    onTextScanned = { text ->
                        navController.navigate("search?query=$text") {
                            popUpTo("scan") { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("randomizer") {
                RandomizerScreen(onBackClick = { navController.popBackStack() })
            }
            
            composable(
                route = "details/{gameId}",
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) {
                DetailsScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = name, style = MaterialTheme.typography.headlineMedium)
        }
    }
}