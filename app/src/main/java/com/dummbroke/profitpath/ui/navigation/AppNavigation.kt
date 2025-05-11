package com.dummbroke.profitpath.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import com.dummbroke.profitpath.ui.trade_detail.TradeDetailScreen
import com.dummbroke.profitpath.ui.trade_entry.TradeEntryScreen
import com.dummbroke.profitpath.ui.trade_history.TradeHistoryScreen
import com.dummbroke.profitpath.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

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
        else -> if (currentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) Screen.SingleTradeView.title else "ProfitPath"
    }

    val showTopAndBottomBars = currentUser != null && 
        (bottomNavItems.any { it.route == currentRoute } || drawerNavItems.any { it.route == currentRoute } || currentRoute?.startsWith(Screen.SingleTradeView.route) == true)

    val showBackButton = showTopAndBottomBars && when (currentRoute) {
        Screen.PerformanceSummary.route,
        Screen.Settings.route,
        Screen.SingleTradeView.route -> navController.previousBackStackEntry != null
        else -> if (currentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) {
            navController.previousBackStackEntry != null
        } else {
            false
        }
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
        else -> if (nestedCurrentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) Screen.SingleTradeView.title else "ProfitPath"
    }

    val actualShowBackButton = when (nestedCurrentRoute) {
        Screen.PerformanceSummary.route,
        Screen.Settings.route,
        Screen.SingleTradeView.route -> mainAppNavController.previousBackStackEntry != null // Use new NavController
        else -> if (nestedCurrentRoute?.startsWith(Screen.SingleTradeView.route + "/") == true) {
            mainAppNavController.previousBackStackEntry != null // Use new NavController
        } else {
            false
        }
    }
    val showBottomNav = bottomNavItems.any { it.route == nestedCurrentRoute }

    ModalNavigationDrawer(
        drawerState = drawerState,
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
                TopAppBar(
                    title = { Text(actualCurrentScreenTitle) },
                    navigationIcon = {
                        if (actualShowBackButton) {
                            IconButton(onClick = { mainAppNavController.popBackStack() }) { // Use new NavController
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
            },
            bottomBar = {
                if (showBottomNav) {
                    AppBottomNavigationBar(
                        currentRoute = nestedCurrentRoute, // Pass nestedCurrentRoute
                        onNavigate = { route ->
                            mainAppNavController.navigate(route) { // Use new NavController
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
                // route = MainAppDestinations.Home // This NavHost doesn't need a graph route itself if it's the content of a scaffold screen
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
                            mainAppNavController.navigate("${Screen.SingleTradeView.route}/$tradeId") // Use new NavController
                        }
                    )
                }
                composable(Screen.PerformanceSummary.route) {
                    PerformanceScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(settingsViewModel = settingsViewModel)
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