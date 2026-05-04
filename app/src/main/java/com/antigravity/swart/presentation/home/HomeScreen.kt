package com.antigravity.swart.presentation.home

import androidx.compose.foundation.background
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
import com.antigravity.swart.presentation.theme.DarkBackground
import com.antigravity.swart.presentation.theme.NeonViolet

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Solo para pruebas: estado local para alternar entre Artista y Usuario
    var currentUserType by remember { mutableStateOf(UserType.ARTIST) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            // El BottomBar personalizado
            SwartBottomNav(
                userType = currentUserType,
                currentRoute = "descubrir",
                onNavigate = {
                    // Para depurar, si tocan el perfil cambiamos el tipo de usuario para ver ambas barras
                    if (it == "perfil") {
                        currentUserType = if (currentUserType == UserType.ARTIST) UserType.GENERAL else UserType.ARTIST
                    }
                },
                onFabClick = { /* Abrir modal de añadir obra */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeTopBar()
            SearchBarComponent()
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = NeonViolet,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    ExhibitionMasonryGrid(
                        exhibitions = uiState.exhibitions,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
