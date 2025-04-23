package com.mtl.My_Hack_X.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mtl.My_Hack_X.navigation.Screen
import com.mtl.My_Hack_X.ui.screens.login.LoginScreen
import com.mtl.My_Hack_X.ui.screens.home.HomeScreen
import com.mtl.My_Hack_X.ui.screens.profile.ProfileScreen
import com.mtl.My_Hack_X.ui.screens.events.EventsScreen
import com.mtl.My_Hack_X.ui.screens.eventdetails.EventDetailsScreen
import com.mtl.My_Hack_X.ui.screens.notifications.NotificationsScreen
import com.mtl.My_Hack_X.ui.screens.admin.AdminScreen
import com.mtl.My_Hack_X.ui.screens.teamdetails.TeamDetailsScreen
import com.mtl.My_Hack_X.ui.screens.splash.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.Events.route) {
            EventsScreen(navController = navController)
        }
        
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(
                eventId = eventId,
                navController = navController
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        
        composable(
            route = Screen.TeamDetails.route,
            arguments = listOf(navArgument("teamId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            TeamDetailsScreen(
                teamId = teamId,
                navController = navController
            )
        }
        
        composable(Screen.Admin.route) {
            AdminScreen(navController = navController)
        }
    }
} 