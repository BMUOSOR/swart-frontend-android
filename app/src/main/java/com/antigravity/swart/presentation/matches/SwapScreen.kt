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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
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
import com.antigravity.swart.presentation.theme.DarkBackground
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import kotlinx.coroutines.launch

@Composable
fun SwapScreen(
    role: String = "interesado",
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onNavigateHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToObras: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val artworks = uiState.artworks

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            val context = LocalContext.current
            val sessionManager = remember { com.antigravity.swart.core.SessionManager(context) }
            val resolvedUserType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL
            SwartBottomNav(
                userType = resolvedUserType,
                currentRoute = "descubrir",
                onNavigate = {
                    when (it) {
                        "home" -> onNavigateHome()
                        "mapa" -> onNavigateToMap()
                        "obras" -> onNavigateToObras()
                        "mensajes" -> onNavigateToMensajes()
                        "favoritos" -> onNavigateToFavoritos()
                        "perfil" -> onLogout()
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
            if (uiState.isLoading && artworks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ArtistaGradientStart)
                }
            } else if (artworks.isNotEmpty()) {
                // Siempre mostramos artworks[0]. El ViewModel elimina la obra cuando se swipea,
                // de modo que el "siguiente" pasa a ser el nuevo índice 0 sin resetear estado.
                val artwork = artworks[0]

                val offsetX = remember { Animatable(0f) }
                val offsetY = remember { Animatable(0f) }
                val rotation = remember { Animatable(0f) }
                val scope = rememberCoroutineScope()

                // Animación de opacidad — se resetea cada vez que cambia la obra mostrada
                val alpha = remember(artwork.id) { Animatable(0f) }
                LaunchedEffect(artwork.id) {
                    alpha.animateTo(1f, animationSpec = tween(800))
                }

                // Colores interpolados para los botones según el Swipe
                val crossRatio = (offsetX.value * -1 / 200f).coerceIn(0f, 1f)
                val heartRatio = (offsetX.value / 200f).coerceIn(0f, 1f)
                val animatedCrossColor = lerp(Color.LightGray, Color.Red, crossRatio)
                val animatedHeartColor = lerp(Color.White, Color.Red, heartRatio)

                // Función de helper para avanzar la carta
                fun swipeCard(isRight: Boolean) {
                    scope.launch {
                        // 1. Animar la tarjeta fuera de pantalla
                        val targetX = if (isRight) 1000f else -1000f
                        offsetX.animateTo(targetX)

                        // 2. El ViewModel elimina la obra de la lista, registra en servidor
                        //    y carga más si quedan pocas — todo en orden garantizado
                        viewModel.recordSwipe(artwork, isRight)

                        // 3. Resetear animación para la nueva carta (que ya es artworks[0])
                        offsetX.snapTo(0f)
                        offsetY.snapTo(0f)
                        rotation.snapTo(0f)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                offsetX.value.toInt(),
                                offsetY.value.toInt()
                            )
                        }
                        .graphicsLayer {
                            rotationZ = rotation.value
                        }
                        .pointerInput(artwork.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        when {
                                            offsetX.value > 150f -> {
                                                swipeCard(isRight = true)
                                            }

                                            offsetX.value < -150f -> {
                                                swipeCard(isRight = false)
                                            }

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
                                        offsetX.snapTo(
                                            offsetX.value + dragAmount.x
                                        )

                                        offsetY.snapTo(
                                            offsetY.value + dragAmount.y
                                        )

                                        rotation.snapTo(
                                            offsetX.value / 15f
                                        )
                                    }
                                }
                            )
                        }
                        .clip(RoundedCornerShape(24.dp))
                ) {

                    // Imagen fondo
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artwork.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay inferior mejor colocado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.92f)
                                    ),
                                    startY = 700f
                                )
                            )
                    )

                    // TOP BAR
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = artwork.artistAvatarUrl,
                                contentDescription = "Artist",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        ArtistaGradientStart,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )

                            Text(
                                text = artwork.artistName,
                                color = Color.White.copy(alpha = alpha.value),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        Color.Black.copy(
                                            alpha = alpha.value * 0.5f
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                            )
                        }

                        if (artwork.isExploration) {

                            Row(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFF9C27B0)
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(4.dp)
                                )

                                Text(
                                    text = "Nuevo Estilo ✨",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                        } else {

                            Row(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF22C55E)
                                            .copy(alpha = 0.9f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Match ${artwork.matchScore.toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // CONTENIDO INFERIOR PEGADO ABAJO
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                        ) {

                            Text(
                                text = artwork.title,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ArtistaGradientStart,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(4.dp)
                                )

                                Text(
                                    text = artwork.locationName
                                        ?: "Lugar desconocido",
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(4.dp)
                                )

                                val distanceMock =
                                    ((artwork.locationAddress?.length ?: 12) / 10.0)

                                Text(
                                    text = "A ${String.format("%.1f", distanceMock)} km",
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }


                // 3. Fila de Botones de Acción (Action Area)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Descartar (Izquierda)
                    IconButton(
                        onClick = { swipeCard(isRight = false) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF1E202C), CircleShape)
                    ) {
                        // Color cambia de LightGray a Rojo al hacer swipe izquierda
                        Icon(Icons.Default.Close, contentDescription = "Descartar", tint = animatedCrossColor, modifier = Modifier.size(32.dp))
                    }

                    // Botón Información (Centro)
                    IconButton(
                        onClick = { onNavigateToDetail(artwork.exhibitionId) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF3B82F6), CircleShape) // Azul vibrante
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Información", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Botón Me Gusta (Derecha)
                    IconButton(
                        onClick = { swipeCard(isRight = true) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(ArtistaGradientStart, CircleShape) // Rosa/Magenta vibrante
                    ) {
                        // Color cambia de White a Rojo al hacer swipe derecha
                        Icon(Icons.Default.Favorite, contentDescription = "Me Gusta", tint = animatedHeartColor, modifier = Modifier.size(32.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No hay más obras por descubrir.", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = ArtistaGradientStart)
                        ) {
                            Text("Buscar más obras")
                        }
                    }
                }
            }
        }
    }
}