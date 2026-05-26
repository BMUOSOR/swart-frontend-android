package com.antigravity.swart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.swart.presentation.home.HomeScreen
import com.antigravity.swart.presentation.detail.DetailScreen
import com.antigravity.swart.presentation.artist.ArtistProfileScreen
import com.antigravity.swart.presentation.matches.SwapScreen
import com.antigravity.swart.presentation.map.MapScreen
import com.antigravity.swart.presentation.profile.UserProfileScreen
import com.antigravity.swart.presentation.auth.LoginScreen
import com.antigravity.swart.presentation.auth.RegisterScreen
import com.antigravity.swart.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.antigravity.swart.presentation.components.UserType
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.antigravity.swart.presentation.exhibitions.EditExhibitionScreen
import com.antigravity.swart.presentation.exhibitions.EditArtworkScreen

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
                    navController.navigate("detail/$exhibitionId?role=$role")
                },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
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
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                }
            )
        }
        composable(
            route = "detail/{exhibitionId}?role={role}",
            arguments = listOf(
                navArgument("exhibitionId") { type = NavType.LongType },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "interesado"
                }
            )
        ) { backStackEntry ->
            val exhibitionId = backStackEntry.arguments?.getLong("exhibitionId") ?: 0L
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            DetailScreen(
                exhibitionId = exhibitionId,
                onBack = { navController.popBackStack() },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                },
                onNavigateToMap = { id ->
                    navController.navigate("mapa/$role?exhibitionId=$id")
                }
            )
        }
        composable("artist_profile/{artistId}") { backStackEntry ->
            ArtistProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("swap/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            
            SwapScreen(
                role = role,
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId?role=$role")
                },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
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
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                }
            )
        }
        composable(
            route = "mapa/{role}?exhibitionId={exhibitionId}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("exhibitionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            val exhibitionId = backStackEntry.arguments?.getLong("exhibitionId") ?: -1L
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
                onNavigateToDetail = { id ->
                    navController.navigate("detail/$id?role=$role")
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                },
                exhibitionIdToSelect = exhibitionId
            )
        }
        composable("perfil/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            UserProfileScreen(
                role = role,
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
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
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
        composable("obras/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "artista"
            com.antigravity.swart.presentation.exhibitions.MyExhibitionsScreen(
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
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId?role=$role")
                },
                onNavigateToEdit = { exhibitionId ->
                    navController.navigate("edit_exhibition/$exhibitionId")
                }
            )
        }
        composable(
            route = "edit_exhibition/{exhibitionId}",
            arguments = listOf(navArgument("exhibitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val role = "artista"
            EditExhibitionScreen(
                onBack = { navController.popBackStack() },
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
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("obras/$role") { inclusive = true }
                    }
                },
                onNavigateToEditArtwork = { artworkId ->
                    navController.navigate("edit_artwork/$artworkId")
                }
            )
        }
        composable(
            route = "edit_artwork/{artworkId}",
            arguments = listOf(navArgument("artworkId") { type = NavType.LongType })
        ) { backStackEntry ->
            val role = "artista"
            EditArtworkScreen(
                onBack = { navController.popBackStack() },
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
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("obras/$role") { inclusive = true }
                    }
                }
            )
        }
    }
}
