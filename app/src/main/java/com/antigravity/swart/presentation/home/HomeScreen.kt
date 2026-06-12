package com.antigravity.swart.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.home.components.ExhibitionMasonryGrid
import com.antigravity.swart.presentation.home.components.HomeTopBar
import com.antigravity.swart.presentation.home.components.SearchBarComponent
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import com.antigravity.swart.presentation.theme.DarkBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

@Composable
fun HomeScreen(
    userType: UserType = UserType.GENERAL,
    role: String = "interesado",
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToMap: (String) -> Unit = {},
    onNavigateToObras: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val filterStartDate by viewModel.filterStartDate.collectAsState()
    val filterEndDate by viewModel.filterEndDate.collectAsState()
    val filterArtistName by viewModel.filterArtistName.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val filterArtworkTag by viewModel.filterArtworkTag.collectAsState()

    val accentColor = if (userType == UserType.ARTIST) ArtistaGradientStart else InteresadoGradientStart

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = androidx.compose.runtime.remember { com.antigravity.swart.core.SessionManager(context) }
            val resolvedUserType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL
            SwartBottomNav(
                userType = resolvedUserType,
                currentRoute = "home",
                onNavigate = {
                    when (it) {
                        "descubrir"  -> onNavigateToSwap()
                        "mapa"       -> onNavigateToMap(role)
                        "obras"      -> onNavigateToObras()
                        "mensajes"   -> onNavigateToMensajes()
                        "favoritos"  -> onNavigateToFavoritos()
                        "perfil"     -> onLogout()
                    }
                },
                onFabClick = onNavigateToCreate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            var showFilterSheet by remember { mutableStateOf(false) }

            // ── HEADER: icono favoritos + título + avatar perfil ─────
            HomeTopBar(
                avatarUrl = userAvatar,
                accentColor = accentColor,
                onNavigateToFavoritos = onNavigateToFavoritos,
                onNavigateToPerfil = onLogout
            )

            // ── BARRA DE BÚSQUEDA: cápsula + sliders ────────────────
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onFilterClick = { showFilterSheet = true },
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── FILTRO BOTTOM SHEET ──────────────────────────────────
            if (showFilterSheet) {
                com.antigravity.swart.presentation.home.components.FilterBottomSheet(
                    initialStartDate = filterStartDate,
                    initialEndDate = filterEndDate,
                    initialArtistName = filterArtistName,
                    initialSelectedTag = selectedTag,
                    initialArtworkTag = filterArtworkTag,
                    accentColor = accentColor,
                    onDismissRequest = { showFilterSheet = false },
                    onApplyFilters = { startDate, endDate, artistName, tag, artworkTag ->
                        viewModel.applyFilters(startDate, endDate, artistName, tag, artworkTag)
                        showFilterSheet = false
                    }
                )
            }

            // ── CONTENIDO PRINCIPAL ──────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            color = accentColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.exhibitions.isEmpty() -> {
                        Text(
                            text = "No se encontraron exposiciones",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        // Grid modernizado con 3 secciones
                        ExhibitionMasonryGrid(
                            exhibitions = uiState.exhibitions,
                            onExhibitionClick = onNavigateToDetail,
                            onArtistClick = onNavigateToArtistProfile,
                            accentColor = accentColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}