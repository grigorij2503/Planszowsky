package pl.pointblank.planszowsky.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.screens.*
import pl.pointblank.planszowsky.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Collection : Screen("collection", "Kolekcja", Icons.Default.Home)
    object DiceRoller : Screen("dice_roller", "Kostki", Icons.Default.Casino)
    object Wishlist : Screen("wishlist", "Wishlist", Icons.Default.Favorite)
    object Profile : Screen("profile", "Profil", Icons.Default.AccountCircle)
}

@Composable
fun PlanszowskyMainContainer(appTheme: AppTheme = AppTheme.MODERN) {
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
                if (appTheme == AppTheme.PIXEL_ART) {
                    RetroNavigationBar(navController, currentDestination)
                } else {
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
                DiceScreen(appTheme)
            }
            composable(Screen.Wishlist.route) { 
                WishlistScreen(
                    appTheme = appTheme,
                    onGameClick = { gameId -> navController.navigate("details/$gameId") }
                )
            }
            composable(Screen.Profile.route) { ProfileScreen(appTheme) }
            
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
                    appTheme = appTheme,
                    initialQuery = initialQuery,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("scan") {
                ScanScreen(
                    appTheme = appTheme,
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
fun RetroNavigationBar(navController: NavHostController, currentDestination: NavDestination?) {
    val items = listOf(Screen.Collection, Screen.DiceRoller, Screen.Wishlist, Screen.Profile)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RetroBackground)
    ) {
        // Dithering top edge
        Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
            val dotSize = 2.dp.toPx()
            for (x in 0 until (size.width / dotSize).toInt()) {
                if (x % 2 == 0) {
                    drawRect(
                        color = RetroBlack,
                        topLeft = Offset(x * dotSize, 0f),
                        size = Size(dotSize, dotSize)
                    )
                }
            }
        }
        
        // Thick top border
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(RetroBlack))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                RetroNavItem(
                    screen = screen,
                    isSelected = isSelected,
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

@Composable
fun RetroNavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (screen) {
                Screen.Collection -> PixelCollectionIcon(isSelected)
                Screen.DiceRoller -> PixelDiceIcon(isSelected)
                Screen.Wishlist -> PixelHeartIcon(isSelected)
                Screen.Profile -> PixelProfileIcon(isSelected)
            }
        }
        
        Text(
            text = screen.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RetroGold else RetroText,
                fontFamily = FontFamily.Monospace
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = name, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
