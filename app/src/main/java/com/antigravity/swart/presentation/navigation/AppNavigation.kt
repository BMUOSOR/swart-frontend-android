package com.antigravity.swart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.swart.presentation.home.HomeScreen
import com.antigravity.swart.presentation.detail.DetailScreen
import com.antigravity.swart.presentation.matches.SwapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId")
                },
                onNavigateToSwap = {
                    navController.navigate("swap") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable("detail/{exhibitionId}") { backStackEntry ->
            val exhibitionId = backStackEntry.arguments?.getString("exhibitionId")?.toLongOrNull() ?: 0L
            DetailScreen(
                exhibitionId = exhibitionId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("swap") {
            SwapScreen(
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId")
                },
                onBack = { 
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
