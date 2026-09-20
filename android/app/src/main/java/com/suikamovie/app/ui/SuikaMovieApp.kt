package com.suikamovie.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import com.google.firebase.auth.FirebaseAuth
import com.suikamovie.app.ui.components.SuikaBottomNavBar
import com.suikamovie.app.ui.navigation.BOTTOM_NAV_ROUTES
import com.suikamovie.app.ui.navigation.Screen
import com.suikamovie.app.ui.screens.account.AccountScreen
import com.suikamovie.app.ui.screens.auth.LoginScreen
import com.suikamovie.app.ui.screens.detail.DetailScreen
import com.suikamovie.app.ui.screens.history.HistoryScreen
import com.suikamovie.app.ui.screens.home.HomeScreen
import com.suikamovie.app.ui.screens.intro.DisclaimerScreen
import com.suikamovie.app.ui.screens.intro.WelcomeScreen
import com.suikamovie.app.ui.screens.search.SearchScreen

/**
 * Root composable & graph navigasi SuikaMovie. Urutan intro: Welcome ->
 * Disclaimer -> (Login kalau belum login) -> Beranda, sama persis kayak
 * flow yang udah difinalin di versi web/WebView dulu.
 */
@Composable
fun SuikaMovieApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in BOTTOM_NAV_ROUTES) {
                SuikaBottomNavBar(currentRoute = currentRoute) { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { inclusive = false; saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    animationSpec = androidx.compose.animation.core.tween(280),
                    initialOffsetX = { it / 4 },
                ) + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(280))
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180))
            },
            popEnterTransition = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220))
            },
            popExitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    animationSpec = androidx.compose.animation.core.tween(280),
                    targetOffsetX = { it / 4 },
                ) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(220))
            },
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(onContinue = {
                    navController.navigate(Screen.Disclaimer.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Disclaimer.route) {
                DisclaimerScreen(onContinue = {
                    val alreadyLoggedIn = FirebaseAuth.getInstance().currentUser != null
                    val destination = if (alreadyLoggedIn) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Disclaimer.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(onLoggedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenDetail = { id, type -> navController.navigate(Screen.Detail.buildRoute(id, type)) },
                    onOpenSearch = { navController.navigate(Screen.Search.route) },
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(onOpenDetail = { id, type -> navController.navigate(Screen.Detail.buildRoute(id, type)) })
            }

            composable(Screen.History.route) {
                HistoryScreen(onOpenDetail = { id, type -> navController.navigate(Screen.Detail.buildRoute(id, type)) })
            }

            composable(Screen.Account.route) {
                AccountScreen(onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("type") { type = androidx.navigation.NavType.StringType },
                ),
            ) { backStackEntry ->
                val idArg = backStackEntry.arguments?.getInt("id") ?: -1
                val typeArg = backStackEntry.arguments?.getString("type") ?: "movie"
                DetailScreen(
                    id = idArg,
                    type = typeArg,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
