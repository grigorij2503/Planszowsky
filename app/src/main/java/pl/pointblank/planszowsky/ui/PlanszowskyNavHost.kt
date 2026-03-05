package pl.pointblank.planszowsky.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.screens.*
import pl.pointblank.planszowsky.ui.theme.*

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Collection : Screen("collection", R.string.menu_collection, Icons.Default.GridView)
    object DiceRoller : Screen("dice_roller", R.string.menu_dice, Icons.Default.Casino)
    object Wishlist : Screen("wishlist", R.string.menu_wishlist, Icons.Default.Favorite)
    object Profile : Screen("profile", R.string.menu_profile, Icons.Default.AccountCircle)
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
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Top // Keep icons aligned at the top
                        ) {
                            val items = listOf(Screen.Collection, Screen.DiceRoller, Screen.Wishlist, Screen.Profile)
                            items.forEach { screen ->
                                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                ModernNavItem(
                                    screen = screen,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Collection.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Collection.route) {
                CollectionScreen(
                    onAddGameClick = { navController.navigate("search") },
                    onGameClick = { gameId, collectionId -> 
                        val encodedId = android.net.Uri.encode(gameId)
                        val encodedColl = android.net.Uri.encode(collectionId)
                        navController.navigate("details/$encodedId/$encodedColl") 
                    },
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
                    onGameClick = { gameId, collectionId -> 
                        val encodedId = android.net.Uri.encode(gameId)
                        val encodedColl = android.net.Uri.encode(collectionId)
                        navController.navigate("details/$encodedId/$encodedColl") 
                    }
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
                route = "details/{gameId}/{collectionId}",
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType },
                    navArgument("collectionId") { type = NavType.StringType; defaultValue = "main" }
                )
            ) {
                DetailsScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun ModernNavItem(screen: Screen, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.heightIn(min = 40.dp), // Increased space for larger font
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(screen.labelRes),
                style = MaterialTheme.typography.labelMedium.copy( // Changed from labelSmall
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = color
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true
            )
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
            .windowInsetsPadding(WindowInsets.navigationBars) // Ensure it respects system nav bar
    ) {
        // Thick top border
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(RetroBlack))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            items.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                RetroNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    modifier = Modifier.weight(1f),
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
        // Extra bottom padding for "safe area"
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun RetroNavItem(screen: Screen, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp), // Restored to 32.dp
            contentAlignment = Alignment.Center
        ) {
            when (screen) {
                Screen.Collection -> PixelCollectionIcon(true)
                Screen.DiceRoller -> PixelDiceIcon(true)
                Screen.Wishlist -> PixelShinyHeartIcon(true)
                Screen.Profile -> PixelProfileIcon(true)
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(screen.labelRes).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp, // Increased from 9.sp
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RetroGold else RetroText,
                fontFamily = FontFamily.Monospace
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
