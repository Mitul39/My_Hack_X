package com.mtl.My_Hack_X.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mtl.My_Hack_X.ui.screens.login.LoginScreen
import com.mtl.My_Hack_X.ui.screens.home.HomeScreen
import com.mtl.My_Hack_X.ui.screens.profile.ProfileScreen
import com.mtl.My_Hack_X.ui.screens.notifications.NotificationsScreen
import com.mtl.My_Hack_X.ui.screens.eventdetails.EventDetailsScreen
import com.mtl.My_Hack_X.ui.screens.splash.SplashScreen
import com.mtl.My_Hack_X.ui.screens.admin.AdminScreen
import com.mtl.My_Hack_X.ui.screens.teamdetails.TeamDetailsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavGraph(val route: String) {
    object Auth : NavGraph("auth")
    object Main : NavGraph("main")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                viewModel = viewModel()
            )
        }
        
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            EventDetailsScreen(
                eventId = eventId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.TeamDetails.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: return@composable
            TeamDetailsScreen(
                teamId = teamId,
                navController = navController
            )
        }
        
        composable(Screen.Admin.route) {
            AdminScreen(navController)
        }
    }
} 