package com.antigravity.swart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.swart.presentation.home.HomeScreen
import com.antigravity.swart.presentation.detail.DetailScreen
import com.antigravity.swart.presentation.matches.SwapScreen
import com.antigravity.swart.presentation.map.MapScreen
import com.antigravity.swart.presentation.auth.LoginScreen
import com.antigravity.swart.presentation.auth.RegisterScreen
import com.antigravity.swart.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.antigravity.swart.presentation.components.UserType
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = "login", route = "auth") {
            composable("login") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("auth")
                }
                val authViewModel = hiltViewModel<AuthViewModel>(parentEntry)
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {
                        val currentRole = authViewModel.role.value
                        navController.navigate("home/$currentRole") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("auth")
                }
                val authViewModel = hiltViewModel<AuthViewModel>(parentEntry)
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        val currentRole = authViewModel.role.value
                        navController.navigate("home/$currentRole") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("home/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            val userType = if (role == "artista") UserType.ARTIST else UserType.GENERAL
            
            HomeScreen(
                userType = userType,
                role = role,
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId")
                },
                onNavigateToSwap = {
                    navController.navigate("swap/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMap = { roleStr ->
                    navController.navigate("mapa/$roleStr")
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
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
        composable("swap/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            
            SwapScreen(
                role = role,
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId")
                },
                onNavigateHome = {
                    navController.navigate("home/$role") {
                        popUpTo("home/$role") { inclusive = true }
                    }
                },
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("mapa/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            val userType = if (role == "artista") UserType.ARTIST else UserType.GENERAL
            
            MapScreen(
                userType = userType,
                onNavigateHome = {
                    navController.navigate("home/$role") {
                        popUpTo("home/$role") { inclusive = true }
                    }
                },
                onNavigateToSwap = {
                    navController.navigate("swap/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId")
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
