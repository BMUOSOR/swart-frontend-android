package com.antigravity.swart.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    exhibitionId: Long,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel() // Reuse home VM for simplicity if it has data
) {
    val uiState by viewModel.uiState.collectAsState()
    val exhibition = uiState.exhibitions.find { it.id == exhibitionId }
    
    var isLiked by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            SwartBottomNav(
                userType = UserType.GENERAL,
                currentRoute = "",
                onNavigate = {},
                onFabClick = {}
            )
        }
    ) { paddingValues ->
        if (exhibition == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonViolet)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Header Image with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(exhibition.exhibitionImgUrl ?: exhibition.artworkImagesUrls.firstOrNull())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Exhibition Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient for top bar visibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    
                    Row {
                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { isLiked = !isLiked },
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (isLiked) Color.Red else Color.Black)
                        }
                    }
                }
            }

            // Main Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Title and "En curso" badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = exhibition.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // En curso badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF3B2A50), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "En curso",
                            color = NeonViolet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Artist
                Text(
                    text = exhibition.artistName,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Place Name
                Text(
                    text = exhibition.nombreLugar ?: "Lugar desconocido",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Sobre la Exposición",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exhibition.description ?: "Sin descripción.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Card (Horarios, Ubicación, Precio)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B38)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Información Práctica",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Horarios
                        Text(text = "Horarios", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "Fecha Inicio: ${exhibition.fechaInicio ?: "N/A"}", color = Color.LightGray, fontSize = 14.sp)
                        Text(text = "Fecha Fin: ${exhibition.fechaFin ?: "N/A"}", color = Color.LightGray, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ubicación
                        Text(text = "Ubicación", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = exhibition.ubicacion ?: "N/A", color = Color.LightGray, fontSize = 14.sp)
                        Text(text = "Ver en mapa →", color = NeonViolet, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Precio
                        Text(text = "Precio", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "${exhibition.precio ?: "Gratis"}€", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Gallery Carousel
                if (exhibition.artworkImagesUrls.isNotEmpty()) {
                    Text(
                        text = "Galería de Obras",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(exhibition.artworkImagesUrls) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Buy Tickets Button
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Conseguir Entradas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
