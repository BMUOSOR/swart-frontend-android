package com.antigravity.swart.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
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
import java.time.LocalDate
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
import com.antigravity.swart.presentation.theme.ArtistaGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    exhibitionId: Long,
    role: String,
    onBack: () -> Unit,
    onNavigateToArtistProfile: (Long) -> Unit,
    onNavigateToMap: (Long) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel() // Reuse home VM for simplicity if it has data
) {
    val uiState by viewModel.uiState.collectAsState()
    val exhibition = uiState.exhibitions.find { it.id == exhibitionId }

    // Incrementa el contador de visitantes una sola vez al entrar
    LaunchedEffect(exhibitionId) {
        viewModel.incrementView(exhibitionId)
    }

    var showTicketsSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            val context = LocalContext.current
            val sessionManager = remember { com.antigravity.swart.core.SessionManager(context) }
            val userType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL
            SwartBottomNav(
                userType = userType,
                currentRoute = "",
                onNavigate = onNavigate,
                onFabClick = {}
            )
        }
    ) { paddingValues ->
        if (exhibition == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArtistaGradientStart)
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
                        .data(exhibition.exhibitionImgUrl ?: exhibition.artworks.firstOrNull()?.imageUrl)
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
                    
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            // Main Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Title and status badge
                val exhibitionStatus = remember(exhibition.fechaInicio, exhibition.fechaFin) {
                    try {
                        val today = LocalDate.now()
                        val start = exhibition.fechaInicio?.let { LocalDate.parse(it) }
                        val end   = exhibition.fechaFin?.let   { LocalDate.parse(it) }
                        when {
                            start != null && today.isBefore(start) -> "PRÓXIMA"
                            end   != null && today.isAfter(end)    -> "FINALIZADA"
                            else -> "ACTIVA"
                        }
                    } catch (e: Exception) {
                        "ACTIVA"
                    }
                }
                val badgeTextColor = when (exhibitionStatus) {
                    "ACTIVA"     -> Color(0xFF10B981) // verde
                    "PRÓXIMA"    -> Color(0xFF818CF8) // índigo claro
                    else         -> Color(0xFF9CA3AF) // gris (FINALIZADA)
                }
                val badgeBgColor = when (exhibitionStatus) {
                    "ACTIVA"     -> Color(0xFF052E16)
                    "PRÓXIMA"    -> Color(0xFF1E1B4B)
                    else         -> Color(0xFF1F2937)
                }

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

                    Box(
                        modifier = Modifier
                            .background(badgeBgColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = exhibitionStatus,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                // Artists (Soporte multi-artista con clicks individuales)
                val artistsList = exhibition.artists
                if (artistsList.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Artistas Creadores",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                        artistsList.forEach { artist ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onNavigateToArtistProfile(artist.id) }
                                    .background(Color(0xFF231B30))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                AsyncImage(
                                    model = artist.avatarUrl,
                                    contentDescription = artist.name,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, ArtistaGradientStart, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = artist.name,
                                    color = ArtistaGradientStart,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToArtistProfile(exhibition.artistId) }
                    ) {
                        AsyncImage(
                            model = exhibition.artistAvatarUrl,
                            contentDescription = "Artist Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, ArtistaGradientStart, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = exhibition.artistName,
                            color = ArtistaGradientStart, // Color de acento rosa neón para indicar que es clickable
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                        Text(
                            text = "Ver en mapa →",
                            color = ArtistaGradientStart,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { onNavigateToMap(exhibition.id) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Precio
                        Text(text = "Precio", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (exhibition.precio != null && exhibition.precio != 0.0)
                                    "${"%.2f".format(exhibition.precio)} €"
                                else
                                    "Gratis",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Gallery Carousel
                if (exhibition.artworks.isNotEmpty()) {
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
                        items(exhibition.artworks) { artwork ->
                            AsyncImage(
                                model = artwork.imageUrl,
                                contentDescription = artwork.title,
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
                    onClick = { showTicketsSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArtistaGradientStart),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Conseguir Entradas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showTicketsSheet && exhibition != null) {
        val safeExhibition = exhibition
        ModalBottomSheet(
            onDismissRequest = { showTicketsSheet = false },
            containerColor = Color(0xFF1E152A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tu Entrada para Swart",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                QrCodePlaceholder(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E223F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = safeExhibition.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lugar: ${safeExhibition.nombreLugar ?: "N/A"}",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Fecha: ${safeExhibition.fechaInicio ?: ""} - ${safeExhibition.fechaFin ?: ""}",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Asistente:",
                            color = ArtistaGradientStart,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Clara Ríos",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QrCodePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size.width
            val cellSize = sizePx / 15f
            
            // Draw Finder Patterns (Corners)
            fun drawFinderPattern(x: Float, y: Float) {
                // Outer square
                drawRect(
                    color = Color.Black,
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(cellSize * 5, cellSize * 5)
                )
                // Inner white area
                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(x + cellSize, y + cellSize),
                    size = androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3)
                )
                // Center black block
                drawRect(
                    color = Color.Black,
                    topLeft = androidx.compose.ui.geometry.Offset(x + cellSize * 1.5f, y + cellSize * 1.5f),
                    size = androidx.compose.ui.geometry.Size(cellSize * 2, cellSize * 2)
                )
            }
            
            // Top Left
            drawFinderPattern(0f, 0f)
            // Top Right
            drawFinderPattern(sizePx - cellSize * 5, 0f)
            // Bottom Left
            drawFinderPattern(0f, sizePx - cellSize * 5)
            
            // Draw some random blocks to simulate QR data
            val random = java.util.Random(42) // deterministic seed
            for (row in 0 until 15) {
                for (col in 0 until 15) {
                    // Skip finder pattern zones
                    if ((row < 6 && col < 6) || (row < 6 && col >= 9) || (row >= 9 && col < 6)) {
                        continue
                    }
                    if (random.nextBoolean()) {
                        drawRect(
                            color = Color.Black,
                            topLeft = androidx.compose.ui.geometry.Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}
