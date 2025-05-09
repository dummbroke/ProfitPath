package com.dummbroke.profitpath.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Import for Back icon
import androidx.compose.material.icons.filled.Menu // Standard Material Icon for burger menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dummbroke.profitpath.R // For your burger menu icon
import com.dummbroke.profitpath.ui.home.HomeScreen
import com.dummbroke.profitpath.ui.performance.PerformanceScreen
import com.dummbroke.profitpath.ui.settings.SettingsScreen // Create this screen file
import com.dummbroke.profitpath.ui.trade_detail.TradeDetailScreen
import com.dummbroke.profitpath.ui.trade_entry.TradeEntryScreen
import com.dummbroke.profitpath.ui.trade_history.TradeHistoryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine the current screen title
    val currentScreen = bottomNavItems.find { it.route == currentRoute } 
                        ?: drawerNavItems.find { it.route == currentRoute }
                        // Add specific handling for routes with arguments if needed for titles
                        ?: if (currentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) Screen.SingleTradeView else null
                        ?: Screen.Home // Default title

    // Revised logic for back button visibility
    val showBackButton = when (currentRoute) {
        Screen.PerformanceSummary.route,
        Screen.Settings.route,
        Screen.SingleTradeView.route -> navController.previousBackStackEntry != null
        else -> if (currentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) {
            navController.previousBackStackEntry != null
        } else {
            false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                drawerItems = drawerNavItems,
                currentRoute = currentRoute,
                navigateToScreen = {
                    navController.navigate(it) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        if (showBackButton) { // Use the new refined logic
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            // No navigation icon on the left if not a designated back screen or cannot pop
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(id = R.drawable.ic_burger_menu_placeholder), contentDescription = "Open Navigation Drawer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface // Ensure action icon color matches
                    )
                )
            },
            bottomBar = {
                val showBottomBarForRoute = bottomNavItems.any { it.route == currentRoute }
                if (showBottomBarForRoute) { // Renamed for clarity
                    AppBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = {
                            navController.navigate(it) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen()
                }
                composable(Screen.TradeEntry.route) {
                    TradeEntryScreen()
                }
                composable(Screen.TradeHistory.route) {
                    TradeHistoryScreen(
                        onTradeClick = { tradeId ->
                            navController.navigate("${Screen.SingleTradeView.route}/$tradeId")
                        }
                    )
                }
                composable(Screen.PerformanceSummary.route) {
                    PerformanceScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen() // Ensure SettingsScreen.kt is created
                }
                composable(Screen.SingleTradeView.route) { 
                    TradeDetailScreen(tradeId = null)
                }
                composable(
                    route = "${Screen.SingleTradeView.route}/{tradeId}",
                    arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tradeId = backStackEntry.arguments?.getString("tradeId")
                    TradeDetailScreen(tradeId = tradeId)
                }
            }
        }
    }
} 