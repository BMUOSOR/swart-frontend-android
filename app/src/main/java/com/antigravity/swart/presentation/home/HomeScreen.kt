package com.antigravity.swart.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.antigravity.swart.presentation.home.components.ExhibitionCard
import com.antigravity.swart.presentation.home.components.ExhibitionMasonryGrid
import com.antigravity.swart.presentation.home.components.HomeTopBar
import com.antigravity.swart.presentation.home.components.SearchBarComponent
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
                    if (it == "descubrir") {
                        onNavigateToSwap()
                    } else if (it == "mapa") {
                        onNavigateToMap(role)
                    } else if (it == "obras") {
                        onNavigateToObras()
                    } else if (it == "mensajes") {
                        onNavigateToMensajes()
                    } else if (it == "perfil") {
                        onLogout()
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

            HomeTopBar(avatarUrl = userAvatar)
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onFilterClick = { showFilterSheet = true }
            )
            
            if (showFilterSheet) {
                com.antigravity.swart.presentation.home.components.FilterBottomSheet(
                    initialStartDate = filterStartDate,
                    initialEndDate = filterEndDate,
                    initialArtistName = filterArtistName,
                    initialSelectedTag = selectedTag,
                    initialArtworkTag = filterArtworkTag,
                    onDismissRequest = { showFilterSheet = false },
                    onApplyFilters = { startDate, endDate, artistName, tag, artworkTag ->
                        viewModel.applyFilters(startDate, endDate, artistName, tag, artworkTag)
                        showFilterSheet = false
                    }
                )
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = InteresadoGradientStart,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.exhibitions.isEmpty()) {
                    Text(
                        text = "No se encontraron exposiciones",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Delegamos todo el layout jerárquico al Grid para que haga scroll en conjunto
                    ExhibitionMasonryGrid(
                        exhibitions = uiState.exhibitions,
                        onExhibitionClick = onNavigateToDetail,
                        onArtistClick = onNavigateToArtistProfile,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
