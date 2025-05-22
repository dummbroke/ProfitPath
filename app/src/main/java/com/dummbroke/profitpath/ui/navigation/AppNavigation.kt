package com.dummbroke.profitpath.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dummbroke.profitpath.R
import com.dummbroke.profitpath.ui.auth.AuthViewModel
import com.dummbroke.profitpath.ui.auth.SignInScreen
import com.dummbroke.profitpath.ui.auth.SignUpScreen
import com.dummbroke.profitpath.ui.home.HomeScreen
import com.dummbroke.profitpath.ui.performance.PerformanceScreen
import com.dummbroke.profitpath.ui.settings.SettingsScreen
import com.dummbroke.profitpath.ui.trade_entry.TradeEntryScreen
import com.dummbroke.profitpath.ui.trade_history.TradeHistoryScreen
import com.dummbroke.profitpath.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import com.dummbroke.profitpath.ui.trade_asset.TradeAssetScreen
import com.dummbroke.profitpath.ui.airdrops.AirdropsScreen

object AuthDestinations {
    const val SignIn = "signIn"
    const val SignUp = "signUp"
}

object MainAppDestinations {
    const val Home = "home_main" // To avoid conflict with Screen.Home if it's just "home"
    // Add other main app graph routes here if you create a nested graph
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController: NavHostController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val authState by authViewModel.authUiState.collectAsState()
    val currentUser = authState.currentUser
    var isLoadingAuthState by remember { mutableStateOf(true) } // To show loading initially

    // Determine start destination after auth state is loaded
    val startDestination = remember(currentUser, isLoadingAuthState) {
        if (isLoadingAuthState) {
            null // Indicate loading, NavHost won't compose yet
        } else if (currentUser != null) {
            MainAppDestinations.Home
        } else {
            AuthDestinations.SignIn
        }
    }

    LaunchedEffect(key1 = currentUser) {
        isLoadingAuthState = false // Auth state has been checked
    }

    if (startDestination == null) {
        // Optional: Show a global loading indicator while determining start destination
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return // Don't compose NavHost until startDestination is determined
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentScreenTitle = when (currentRoute) {
        AuthDestinations.SignIn -> "Sign In"
        AuthDestinations.SignUp -> "Sign Up"
        Screen.Home.route -> Screen.Home.title
        Screen.TradeEntry.route -> Screen.TradeEntry.title
        Screen.TradeHistory.route -> Screen.TradeHistory.title
        Screen.PerformanceSummary.route -> Screen.PerformanceSummary.title
        Screen.Settings.route -> Screen.Settings.title
        Screen.TradeAsset.route -> Screen.TradeAsset.title
        else -> "ProfitPath"
    }

    val showTopAndBottomBars = currentUser != null && 
        (bottomNavItems.any { it.route == currentRoute } || 
         drawerNavItems.any { it.route == currentRoute })

    val showBackButton = showTopAndBottomBars && when (currentRoute) {
        Screen.PerformanceSummary.route,
        Screen.Settings.route -> navController.previousBackStackEntry != null
        else -> false
    }

    val onLogoutConfirmed = {
        authViewModel.signOut()
        navController.navigate(AuthDestinations.SignIn) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier // Padding will be applied by Scaffold if used
    ) {
        // --- Auth Graph ---
        composable(AuthDestinations.SignIn) {
            SignInScreen(
                authViewModel = authViewModel,
                onSignInSuccess = {
                    navController.navigate(MainAppDestinations.Home) {
                        popUpTo(AuthDestinations.SignIn) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(AuthDestinations.SignUp) }
            )
        }
        composable(AuthDestinations.SignUp) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(MainAppDestinations.Home) {
                        popUpTo(AuthDestinations.SignUp) { inclusive = true }
                    }
                },
                onNavigateToSignIn = { navController.popBackStack() } // Go back to SignIn
            )
        }

        // --- Main App Graph ---
        // This composable acts as the entry for the main app features after login
        composable(MainAppDestinations.Home) {
            // Pass AuthViewModel for sign out, SettingsViewModel for logout event
            val settingsViewModel: SettingsViewModel = viewModel() 
            LaunchedEffect(Unit) {
                settingsViewModel.logoutConfirmedEvent.collect {
                    authViewModel.signOut() // Perform actual sign out
                    navController.navigate(AuthDestinations.SignIn) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
            MainAppScaffold(
                drawerState = drawerState, 
                scope = scope, 
                settingsViewModel = settingsViewModel // Pass it down
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    drawerState: DrawerState, 
    scope: kotlinx.coroutines.CoroutineScope,
    settingsViewModel: SettingsViewModel
) {
    val mainAppNavController: NavHostController = rememberNavController() // New NavController for main app screens

    val nestedNavBackStackEntry by mainAppNavController.currentBackStackEntryAsState() // Observe new NavController
    val nestedCurrentRoute = nestedNavBackStackEntry?.destination?.route

    // Recalculate title and back button based on the nested navigation state
    val actualCurrentScreenTitle = when (nestedCurrentRoute) {
        Screen.Home.route -> Screen.Home.title
        Screen.TradeEntry.route -> Screen.TradeEntry.title
        Screen.TradeHistory.route -> Screen.TradeHistory.title
        Screen.PerformanceSummary.route -> Screen.PerformanceSummary.title
        Screen.Settings.route -> Screen.Settings.title
        Screen.TradeAsset.route -> "Manage Assets"
        Screen.Airdrops.route -> "Airdrops"
        else -> "ProfitPath"
    }

    val actualShowBackButton = when (nestedCurrentRoute) {
        Screen.PerformanceSummary.route,
        Screen.Settings.route,
        Screen.TradeAsset.route,
        Screen.Airdrops.route -> mainAppNavController.previousBackStackEntry != null
        else -> false
    }
    val showBottomNav = bottomNavItems.any { it.route == nestedCurrentRoute }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !(nestedCurrentRoute?.startsWith("edit_trade?tradeId=") == true),
        drawerContent = {
            AppDrawerContent(
                drawerItems = drawerNavItems,
                currentRoute = nestedCurrentRoute, // Pass nestedCurrentRoute
                navigateToScreen = { route ->
                    scope.launch { drawerState.close() }
                    mainAppNavController.navigate(route) { // Use new NavController
                        popUpTo(mainAppNavController.graph.findStartDestination().id) {
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
                val isEditScreen = nestedCurrentRoute?.startsWith("edit_trade?tradeId=") == true
                if (isEditScreen) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { mainAppNavController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {},
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                } else {
                    TopAppBar(
                        title = { Text(actualCurrentScreenTitle) },
                        navigationIcon = {
                            if (actualShowBackButton) {
                                IconButton(onClick = { mainAppNavController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
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
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            bottomBar = {
                val isEditScreen = nestedCurrentRoute?.startsWith("edit_trade?tradeId=") == true
                if (showBottomNav && !isEditScreen) {
                    AppBottomNavigationBar(
                        currentRoute = nestedCurrentRoute, // Pass nestedCurrentRoute
                        onNavigate = { route ->
                            mainAppNavController.navigate(route) {
                                popUpTo(mainAppNavController.graph.findStartDestination().id) {
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
                navController = mainAppNavController, // Use the new, dedicated NavController
                startDestination = Screen.Home.route, 
                modifier = Modifier.padding(innerPadding)
            ) {
                // Home Screen
                composable(Screen.Home.route) {
                    HomeScreen(navController = mainAppNavController)
                }
                // Trade Entry Screen (new entry only)
                composable(Screen.TradeEntry.route) {
                    TradeEntryScreen(navController = mainAppNavController, tradeId = null)
                }
                // Trade History Screen
                composable(Screen.TradeHistory.route) {
                    TradeHistoryScreen(navController = mainAppNavController)
                }
                // Edit Trade (special route, not in bottom nav)
                composable("edit_trade?tradeId={tradeId}",
                    arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tradeId = backStackEntry.arguments?.getString("tradeId") ?: ""
                    com.dummbroke.profitpath.ui.trade_entry.EditTradeScreen(navController = mainAppNavController, tradeId = tradeId)
                }
                // Other screens (performance, settings, etc.)
                composable(Screen.PerformanceSummary.route) {
                    PerformanceScreen()
                }
                // Modified Settings route to include optional scrollTo argument
                composable(
                    route = Screen.Settings.route + "?scrollTo={scrollTo}",
                    arguments = listOf(navArgument("scrollTo") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val scrollToTarget = backStackEntry.arguments?.getString("scrollTo")
                    SettingsScreen(settingsViewModel = settingsViewModel, scrollToTarget = scrollToTarget)
                }
                composable(Screen.TradeAsset.route) {
                    com.dummbroke.profitpath.ui.trade_asset.TradeAssetScreen()
                }
                composable(Screen.Airdrops.route) {
                    AirdropsScreen()
                }
                // Removed SingleTradeView navigation
            }
        }
    }
}

// Note: You might need to adjust your Screen object and bottomNavItems/drawerNavItems if their routes
// need to be more specific or if they were conflicting with the new AuthDestinations.
// Example: Screen.Home.route should be distinct if MainAppDestinations.Home is used as a graph route.
// For this implementation, I've made MainAppDestinations.Home the route for the scaffolded content,
// and the nested NavHost starts with Screen.Home.route.

// Revised logic for back button visibility
// val showBackButton = when (currentRoute) {  -- REMOVE THIS LINE
//     Screen.PerformanceSummary.route,          -- REMOVE THIS LINE
//     Screen.Settings.route,                    -- REMOVE THIS LINE
//     Screen.SingleTradeView.route -> navController.previousBackStackEntry != null -- REMOVE THIS LINE
//     else -> if (currentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) { -- REMOVE THIS LINE
//         navController.previousBackStackEntry != null -- REMOVE THIS LINE
//     } else {                                  -- REMOVE THIS LINE
//         false                                 -- REMOVE THIS LINE
//     }                                         -- REMOVE THIS LINE
// }                                               -- REMOVE THIS LINE 