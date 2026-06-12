package com.antigravity.swart.presentation.matches

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.DarkBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientEnd
import com.antigravity.swart.presentation.theme.InteresadoGradientStart
import com.antigravity.swart.presentation.theme.TextGray
import com.antigravity.swart.presentation.theme.TextWhite
import kotlinx.coroutines.launch

// Modos de filtro disponibles en el header
private val FILTER_LABELS = listOf("Todas", "Cerca de ti", "Artistas favoritos")

@Composable
fun SwapScreen(
    role: String = "interesado",
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onNavigateHome: () -> Unit,
    onNavigateToMap: (Long) -> Unit,        // Long = exhibitionId (-1L = sin selección)
    onNavigateToObras: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val artworks = uiState.artworks

    // Avatar del usuario desde SessionManager
    val context = LocalContext.current
    val sessionManager = remember { com.antigravity.swart.core.SessionManager(context) }
    val userAvatarUrl = remember { sessionManager.getImgUrl() }

    // Modo de filtro activo (0=Todas, 1=Cerca de ti, 2=Artistas favoritos)
    var filterIndex by remember { mutableIntStateOf(0) }

    // Cuando cambia el modo: recargar y mezclar el feed
    LaunchedEffect(filterIndex) {
        viewModel.loadFeed()
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            val resolvedUserType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL
            SwartBottomNav(
                userType = resolvedUserType,
                currentRoute = "descubrir",
                onNavigate = {
                    when (it) {
                        "home"      -> onNavigateHome()
                        "mapa"      -> onNavigateToMap(-1L)
                        "obras"     -> onNavigateToObras()
                        "mensajes"  -> onNavigateToMensajes()
                        "favoritos" -> onNavigateToFavoritos()
                        "perfil"    -> onLogout()
                    }
                },
                onFabClick = onNavigateToCreate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading && artworks.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InteresadoGradientStart)
                    }
                }

                artworks.isNotEmpty() -> {
                    val artwork = artworks[0]

                    val offsetX  = remember { Animatable(0f) }
                    val offsetY  = remember { Animatable(0f) }
                    val rotation = remember { Animatable(0f) }
                    val scope    = rememberCoroutineScope()

                    val alpha = remember(artwork.id) { Animatable(0f) }
                    LaunchedEffect(artwork.id) {
                        alpha.animateTo(1f, animationSpec = tween(800))
                    }

                    val crossRatio         = (offsetX.value * -1 / 200f).coerceIn(0f, 1f)
                    val heartRatio         = (offsetX.value / 200f).coerceIn(0f, 1f)
                    val animatedCrossColor = lerp(Color.White, Color.Red, crossRatio)
                    val animatedHeartColor = lerp(Color.White, Color(0xFFEC4899), heartRatio)

                    fun swipeCard(isRight: Boolean) {
                        scope.launch {
                            val targetX = if (isRight) 1200f else -1200f
                            offsetX.animateTo(targetX, animationSpec = tween(300))
                            viewModel.recordSwipe(artwork, isRight)
                            offsetX.snapTo(0f)
                            offsetY.snapTo(0f)
                            rotation.snapTo(0f)
                        }
                    }

                    // ── CABECERA ─────────────────────────────────────────
                    SwipeHeader(
                        userAvatarUrl = userAvatarUrl,
                        filterIndex   = filterIndex,
                        onFilterChange = { newIndex -> filterIndex = newIndex }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── CARTA PRINCIPAL (squircle, sin mazo detrás) ──────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        offsetX.value.toInt(),
                                        offsetY.value.toInt()
                                    )
                                }
                                .graphicsLayer { rotationZ = rotation.value }
                                .pointerInput(artwork.id) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                when {
                                                    offsetX.value > 150f  -> swipeCard(isRight = true)
                                                    offsetX.value < -150f -> swipeCard(isRight = false)
                                                    else -> {
                                                        launch { offsetX.animateTo(0f) }
                                                        launch { offsetY.animateTo(0f) }
                                                        launch { rotation.animateTo(0f) }
                                                    }
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            scope.launch {
                                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                                offsetY.snapTo(offsetY.value + dragAmount.y)
                                                rotation.snapTo(offsetX.value / 15f)
                                            }
                                        }
                                    )
                                }
                                .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp))
                                .clip(RoundedCornerShape(32.dp))
                        ) {
                            // Imagen de fondo
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(artwork.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Artwork",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradiente oscuro inferior
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                            startY = 600f
                                        )
                                    )
                            )

                            // ── TOP: avatar artista + badge ─────────────
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar artista con ring + nombre con fade
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .border(2.dp, InteresadoGradientStart, CircleShape)
                                            .padding(2.dp)
                                    ) {
                                        AsyncImage(
                                            model = artwork.artistAvatarUrl,
                                            contentDescription = "Artista",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = artwork.artistName,
                                        color = Color.White.copy(alpha = alpha.value),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Badge cápsula: exploración o match%
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (artwork.isExploration)
                                                InteresadoGradientEnd.copy(alpha = 0.75f)
                                            else
                                                Color(0xFF22C55E).copy(alpha = 0.80f),
                                            shape = RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (artwork.isExploration) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Nuevo Estilo ✨", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("Match ${artwork.matchScore.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // ── BOTTOM: pastilla glassmorphism ──────────
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color.White.copy(alpha = 0.12f),
                                            RoundedCornerShape(50.dp)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                                        .padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Info: lugar + título obra
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = InteresadoGradientStart,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = artwork.locationName ?: "Lugar desconocido",
                                                color = TextGray,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = artwork.title,
                                            color = TextWhite,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Píldora distancia → abre mapa con la expo seleccionada
                                    val distanceMock = ((artwork.locationAddress?.length ?: 12) / 10.0)
                                    Box(
                                        modifier = Modifier
                                            .background(InteresadoGradientStart, RoundedCornerShape(50.dp))
                                            .clickable { onNavigateToMap(artwork.exhibitionId) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.1f", distanceMock)} km",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── PANEL DE ACCIONES ────────────────────────────────
                    ActionButtonsPanel(
                        onDiscard  = { swipeCard(isRight = false) },
                        onInfo     = { onNavigateToDetail(artwork.exhibitionId) },
                        onLike     = { swipeCard(isRight = true) },
                        crossColor = animatedCrossColor,
                        heartColor = animatedHeartColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                else -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay más obras por descubrir.", color = TextWhite, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.loadFeed() },
                                colors = ButtonDefaults.buttonColors(containerColor = InteresadoGradientStart),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Buscar más obras", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// CABECERA: avatar usuario + título centrado + puntos interactivos
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SwipeHeader(
    userAvatarUrl: String,
    filterIndex: Int,
    onFilterChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Avatar del usuario en esquina superior izquierda con ring de acento
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .border(2.dp, InteresadoGradientStart, CircleShape)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = userAvatarUrl.ifBlank {
                    "https://bkrmqkpxidmemzxhefoc.supabase.co/storage/v1/object/public/Imagenes/usuario_chica_3.jpg"
                },
                contentDescription = "Mi perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )
        }

        // Título y puntos indicadores centrados
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Descubrir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = FILTER_LABELS[filterIndex],
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Tres puntos interactivos: el activo es cápsula, los demás círculos pequeños
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FILTER_LABELS.forEachIndexed { index, _ ->
                    val isActive = index == filterIndex
                    Box(
                        modifier = Modifier
                            .then(
                                if (isActive) Modifier
                                    .height(7.dp)
                                    .width(20.dp)
                                    .background(TextWhite, RoundedCornerShape(50.dp))
                                else Modifier
                                    .size(6.dp)
                                    .background(TextGray.copy(alpha = 0.45f), CircleShape)
                            )
                            .clickable { onFilterChange(index) }
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// PANEL DE ACCIONES: cápsula oscura con 3 botones circulares
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActionButtonsPanel(
    onDiscard:  () -> Unit,
    onInfo:     () -> Unit,
    onLike:     () -> Unit,
    crossColor: Color,
    heartColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(color = Color(0xFF1A1A24), shape = RoundedCornerShape(50.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // X — descartar
            IconButton(
                onClick = onDiscard,
                modifier = Modifier.size(60.dp).background(Color(0xFF2C2C3A), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Descartar", tint = crossColor, modifier = Modifier.size(28.dp))
            }

            // Info — ver exposición
            IconButton(
                onClick = onInfo,
                modifier = Modifier.size(54.dp).background(InteresadoGradientStart, CircleShape)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Ver exposición", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // Corazón — me gusta
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(60.dp).background(Color(0xFF2C2C3A), CircleShape)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Me gusta", tint = heartColor, modifier = Modifier.size(28.dp))
            }
        }
    }
}