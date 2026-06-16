package com.antigravity.swart.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.DiscoverArtwork
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.*

@Composable
fun FavoritosScreen(
    role: String = "interesado",
    onNavigateHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: FavoritosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val resolvedUserType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToMensajes,
                containerColor = InteresadoGradientStart,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Mis chats"
                )
            }
        },
        bottomBar = {
            SwartBottomNav(
                userType = resolvedUserType,
                currentRoute = "favoritos",
                onNavigate = { route ->
                    when (route) {
                        "home"      -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa"      -> onNavigateToMap()
                        "mensajes"  -> onNavigateToMensajes()
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
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Favoritos",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.loadFavoritos() }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Recargar",
                        tint = TextGray
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }

                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error al cargar favoritos",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.loadFavoritos() }) {
                                Text("Reintentar", color = InteresadoGradientStart)
                            }
                        }
                    }
                }

                uiState.artworks.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes favoritos",
                                color = TextGray,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Desliza obras en Descubrir para guardarlas aquí",
                                color = TextGray.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = "${uiState.artworks.size} obra${if (uiState.artworks.size != 1) "s" else ""} guardada${if (uiState.artworks.size != 1) "s" else ""}",
                        color = TextGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.artworks, key = { it.id }) { artwork ->
                            FavoritoCard(
                                artwork = artwork,
                                chatLoading = uiState.chatLoading,
                                onClick = { onNavigateToDetail(artwork.exhibitionId) },
                                onRemove = { viewModel.removeLike(artwork.id) },
                                onChat = { message ->
                                    viewModel.startChat(artwork, message) { chatId ->
                                        onNavigateToChat(chatId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritoCard(
    artwork: DiscoverArtwork,
    chatLoading: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onChat: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var chatMessage by remember(artwork.id) {
        mutableStateOf("¡Hola! Me ha gustado tu obra \"${artwork.title}\". ¿Podemos hablar sobre ella?")
    }

    // Diálogo de confirmación de borrado
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardBackground,
            title = {
                Text("¿Eliminar favorito?", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Se quitará \"${artwork.title}\" de tus favoritos.",
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onRemove()
                }) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    // Diálogo para escribir el mensaje antes de chatear
    if (showChatDialog) {
        AlertDialog(
            onDismissRequest = { showChatDialog = false },
            containerColor = CardBackground,
            title = {
                Text(
                    "Mensaje a ${artwork.artistName}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Escribe el mensaje con el que iniciarás la conversación:",
                        color = TextGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = chatMessage,
                        onValueChange = { chatMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = {
                            Text("Escribe tu mensaje...", color = TextGray.copy(alpha = 0.5f))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = InteresadoGradientStart,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.4f),
                            cursorColor = InteresadoGradientStart,
                            focusedContainerColor = Color(0xFF1E1E2E),
                            unfocusedContainerColor = Color(0xFF1E1E2E)
                        ),
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChatDialog = false
                        onChat(chatMessage)
                    },
                    enabled = chatMessage.isNotBlank() && !chatLoading
                ) {
                    if (chatLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = InteresadoGradientStart,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar", color = InteresadoGradientStart, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatDialog = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {
            // Imagen con overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artwork.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradiente inferior suave
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                )
                // Icono de corazón (top-end)
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Info + acciones
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = artwork.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artwork.artistName,
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Chat con el artista
                    FilledTonalButton(
                        onClick = { showChatDialog = true },
                        enabled = !chatLoading && artwork.artistId != 0L,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = InteresadoGradientStart.copy(alpha = 0.2f),
                            contentColor = InteresadoGradientStart
                        )
                    ) {
                        if (chatLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = InteresadoGradientStart,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "Chatear",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 11.sp)
                        }
                    }

                    // Eliminar favorito
                    FilledTonalButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.Red.copy(alpha = 0.15f),
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quitar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
