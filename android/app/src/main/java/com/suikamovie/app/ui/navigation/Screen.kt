package com.suikamovie.app.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Disclaimer : Screen("disclaimer")
    object Login : Screen("login")

    object Home : Screen("home")
    object Search : Screen("search")
    object History : Screen("history")
    object Account : Screen("account")

    object Detail : Screen("detail/{id}/{type}") {
        fun buildRoute(id: Int, type: String) = "detail/$id/$type"
    }
}

/** Route yang nampilin bottom navigation bar. */
val BOTTOM_NAV_ROUTES = setOf(Screen.Home.route, Screen.Search.route, Screen.Account.route)
