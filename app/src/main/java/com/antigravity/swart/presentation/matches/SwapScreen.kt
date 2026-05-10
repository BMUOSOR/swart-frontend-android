package com.antigravity.swart.presentation.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.home.HomeViewModel
import com.antigravity.swart.presentation.theme.DarkBackground
import com.antigravity.swart.presentation.theme.NeonViolet

@Composable
fun SwapScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exhibitions = uiState.exhibitions
    
    // Flatten artworks for swiping
    val artworks = remember(exhibitions) {
        exhibitions.flatMap { expo ->
            expo.artworkImagesUrls.map { url ->
                Pair(expo, url)
            }
        }
    }
    
    var currentIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            SwartBottomNav(
                userType = UserType.GENERAL,
                currentRoute = "descubrir",
                onNavigate = {
                    if (it == "inicio") onBack()
                },
                onFabClick = {}
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentIndex < artworks.size) {
                val (exhibition, imageUrl) = artworks[currentIndex]

                // Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                    startY = 500f
                                )
                            )
                    )

                    // Info Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = exhibition.artistAvatarUrl,
                                contentDescription = "Artist",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Obra en Exposición",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = exhibition.nombreLugar ?: "Lugar desconocido",
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "A 1.2 km", // Mockup distance
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Buttons (Swipe Left, Info, Swipe Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dislike
                    IconButton(
                        onClick = { currentIndex++ },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF2A2A35), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dislike", tint = Color.LightGray, modifier = Modifier.size(32.dp))
                    }

                    // Info
                    IconButton(
                        onClick = { onNavigateToDetail(exhibition.id) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF3B82F6), CircleShape)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Like
                    IconButton(
                        onClick = { currentIndex++ },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFF43F5E), CircleShape)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay más obras para mostrar.", color = Color.White)
                }
            }
        }
    }
}
