package com.mtl.My_Hack_X.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Events : Screen("events")
    object EventDetails : Screen("event/{eventId}") {
        fun createRoute(eventId: String) = "event/$eventId"
    }
    object Notifications : Screen("notifications")
    object TeamDetails : Screen("team_details/{teamId}") {
        fun createRoute(teamId: String) = "team_details/$teamId"
    }
    object Admin : Screen("admin")
} 