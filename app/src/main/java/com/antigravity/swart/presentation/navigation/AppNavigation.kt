package com.antigravity.swart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.antigravity.swart.presentation.home.HomeScreen
import com.antigravity.swart.presentation.detail.DetailScreen
import com.antigravity.swart.presentation.artist.ArtistProfileScreen
import com.antigravity.swart.presentation.matches.SwapScreen
import com.antigravity.swart.presentation.map.MapScreen
import com.antigravity.swart.presentation.map.BalizaVaciaDetailScreen
import com.antigravity.swart.presentation.profile.UserProfileScreen
import com.antigravity.swart.presentation.profile.ArtistsListScreen
import com.antigravity.swart.presentation.auth.LoginScreen
import com.antigravity.swart.presentation.auth.RegisterScreen
import com.antigravity.swart.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.antigravity.swart.presentation.components.UserType
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.antigravity.swart.presentation.exhibitions.EditExhibitionScreen
import com.antigravity.swart.presentation.exhibitions.EditArtworkScreen
import com.antigravity.swart.presentation.chat.ChatScreen
import com.antigravity.swart.presentation.favorites.FavoritosScreen

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
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFavoritos = {
                    navController.navigate("favoritos/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
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
                role = role,
                onBack = { navController.popBackStack() },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                },
                onNavigateToMap = { id ->
                    navController.navigate("mapa/$role?exhibitionId=$id")
                },
                onNavigate = { target ->
                    val route = when (target) {
                        "descubrir" -> "swap"
                        else        -> target
                    }
                    navController.navigate("$route/$role") {
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("artist_profile/{artistId}") { backStackEntry ->
            ArtistProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId?role=artista")
                },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat/$chatId")
                }
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
                onNavigateToMap = { exhibitionId ->
                    if (exhibitionId >= 0L) {
                        navController.navigate("mapa/$role?exhibitionId=$exhibitionId") {
                            popUpTo("home/$role") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.navigate("mapa/$role") {
                            popUpTo("home/$role") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFavoritos = {
                    navController.navigate("favoritos/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
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
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFavoritos = {
                    navController.navigate("favoritos/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role")
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
                },
                onNavigateToCreateExhibition = { lat, lon, balizaId ->
                    navController.navigate("create_exhibition?lat=$lat&lon=$lon&balizaId=$balizaId")
                },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                },
                onNavigateToBalizaDetail = { balizaId ->
                    navController.navigate("baliza_vacia_detail/$balizaId")
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
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToMap = {
                    navController.navigate("mapa/$role") {
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") {
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToFavorites = {
                    navController.navigate("favoritos/$role") {
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToArtistas = {
                    navController.navigate("artistas")
                },
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
                }
            )
        }

        composable("obras/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "artista"
            val successMessage by backStackEntry.savedStateHandle.getStateFlow<String?>("success_message", null).collectAsState()
            com.antigravity.swart.presentation.exhibitions.MyExhibitionsScreen(
                successMessage = successMessage,
                onClearSuccessMessage = {
                    backStackEntry.savedStateHandle["success_message"] = null
                },
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
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
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
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
                }
            )
        }
        composable(
            route = "edit_exhibition/{exhibitionId}",
            arguments = listOf(navArgument("exhibitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val role = "artista"
            val successMessage by backStackEntry.savedStateHandle.getStateFlow<String?>("success_message", null).collectAsState()
            EditExhibitionScreen(
                successMessage = successMessage,
                onClearSuccessMessage = {
                    backStackEntry.savedStateHandle["success_message"] = null
                },
                onBack = { navController.popBackStack() },
                onBackWithResult = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                    navController.popBackStack()
                },
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
                },
                onNavigateToCreateArtwork = {
                    val exhibitionId = backStackEntry.arguments?.getLong("exhibitionId") ?: 0L
                    navController.navigate("create_artwork/$exhibitionId")
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
                onBackWithResult = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                    navController.popBackStack()
                },
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

        // ─── NUEVA EXPO ───────────────────────────────────────────────────────
        composable(
            route = "create_exhibition?lat={lat}&lon={lon}&balizaId={balizaId}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; defaultValue = "" },
                navArgument("lon") { type = NavType.StringType; defaultValue = "" },
                navArgument("balizaId") { type = NavType.StringType; defaultValue = "-1" }
            )
        ) {
            val role = "artista"
            com.antigravity.swart.presentation.exhibitions.CreateExhibitionScreen(
                onBack = { navController.popBackStack() },
                onBackWithResult = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate("home/$role") { popUpTo("home/$role") { inclusive = true } }
                },
                onNavigateToSwap = {
                    navController.navigate("swap/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToMap = {
                    navController.navigate("mapa/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") { popUpTo("obras/$role") { inclusive = true } }
                },
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ─── NUEVA OBRA ───────────────────────────────────────────────────────
        composable(
            route = "create_artwork/{exhibitionId}",
            arguments = listOf(navArgument("exhibitionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val role = "artista"
            com.antigravity.swart.presentation.exhibitions.CreateArtworkScreen(
                onBack = { navController.popBackStack() },
                onBackWithResult = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("success_message", message)
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate("home/$role") { popUpTo("home/$role") { inclusive = true } }
                },
                onNavigateToSwap = {
                    navController.navigate("swap/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToMap = {
                    navController.navigate("mapa/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") { popUpTo("obras/$role") { inclusive = true } }
                }
            )
        }

        // ─── MENSAJES / INVITACIONES ──────────────────────────────────────────
        composable(
            route = "mensajes/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "artista" })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "artista"
            com.antigravity.swart.presentation.exhibitions.InvitationsScreen(
                role = role,
                onBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate("home/$role") { popUpTo("home/$role") { inclusive = true } }
                },
                onNavigateToSwap = {
                    navController.navigate("swap/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToMap = {
                    navController.navigate("mapa/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToProfile = {
                    navController.navigate("perfil/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToObras = {
                    navController.navigate("obras/$role") { popUpTo("obras/$role") { inclusive = true } }
                },
                onNavigateToFavoritos = {
                    navController.navigate("favoritos/$role") { popUpTo("home/$role") { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToCreate = {
                    navController.navigate("create_exhibition")
                },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat/$chatId")
                }
            )
        }

        composable(
            route = "chat/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.LongType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getLong("chatId") ?: 0L
            ChatScreen(
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── ARTISTAS SEGUIDOS ───────────────────────────────────────────────
        composable("artistas") {
            ArtistsListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat/$chatId")
                }
            )
        }

        // ─── FAVORITOS ────────────────────────────────────────────────────────
        composable("favoritos/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "interesado"
            FavoritosScreen(
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
                onNavigateToDetail = { exhibitionId ->
                    navController.navigate("detail/$exhibitionId?role=$role")
                },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat/$chatId")
                },
                onNavigateToMensajes = {
                    navController.navigate("mensajes/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate("perfil/$role") {
                        popUpTo("home/$role") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ─── BALIZA VACÍA DETAIL ──────────────────────────────────────────────────
        composable(
            route = "baliza_vacia_detail/{balizaId}",
            arguments = listOf(navArgument("balizaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val balizaId = backStackEntry.arguments?.getLong("balizaId") ?: -1L
            BalizaVaciaDetailScreen(
                balizaId = balizaId,
                onBack = { navController.popBackStack() },
                onNavigateToProposal = { navController.navigate("mapa/artista?balizaId=$balizaId") },
                onNavigateToArtistProfile = { artistId ->
                    navController.navigate("artist_profile/$artistId")
                }
            )
        }
    }
}
