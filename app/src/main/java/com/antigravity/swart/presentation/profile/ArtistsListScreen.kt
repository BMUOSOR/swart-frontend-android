package com.antigravity.swart.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.data.remote.dto.ArtistFollowDto
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsListScreen(
    onBack: () -> Unit,
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    viewModel: ArtistsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recargar al volver de ArtistProfileScreen para sincronizar el estado de follow
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadArtists()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Separate followed and not followed artists
    val followedArtists = uiState.artists.filter { it.following }
    val otherArtists = uiState.artists.filter { !it.following }

    val neonPinkToPurple = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    )

    Scaffold(
        containerColor = Color(0xFF0B0D17),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Artistas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0D17)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "Error desconocido",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.artists.isEmpty()) {
                Text(
                    text = "No se encontraron artistas",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (followedArtists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Siguiendo",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(followedArtists) { artist ->
                            ArtistItem(
                                artist = artist,
                                chatLoading = uiState.chatLoading,
                                onFollowToggle = { viewModel.toggleFollow(artist.id) },
                                onNavigateToProfile = { onNavigateToArtistProfile(artist.id) },
                                onChat = {
                                    viewModel.startChat(artist.id) { chatId ->
                                        onNavigateToChat(chatId)
                                    }
                                }
                            )
                        }
                    }

                    if (otherArtists.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Descubrir más artistas",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(otherArtists) { artist ->
                            ArtistItem(
                                artist = artist,
                                chatLoading = uiState.chatLoading,
                                onFollowToggle = { viewModel.toggleFollow(artist.id) },
                                onNavigateToProfile = { onNavigateToArtistProfile(artist.id) },
                                onChat = {
                                    viewModel.startChat(artist.id) { chatId ->
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
fun ArtistItem(
    artist: ArtistFollowDto,
    chatLoading: Boolean = false,
    onFollowToggle: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onChat: () -> Unit = {}
) {
    val neonPinkToPurple = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToProfile() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + follow badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onFollowToggle
                    )
            ) {
                AsyncImage(
                    model = artist.avatarUrl ?: "https://ui-avatars.com/api/?name=${artist.nombre.replace(" ", "+")}&background=random",
                    contentDescription = artist.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = if (artist.following) neonPinkToPurple else Brush.linearGradient(
                                listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f))
                            ),
                            shape = CircleShape
                        )
                )

                // Follow/Unfollow badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(if (artist.following) Color(0xFF10B981) else Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (artist.following) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (artist.following) "Siguiendo" else "Seguir",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.nombre,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (artist.following) "Siguiendo" else "Toca para ver perfil",
                    color = if (artist.following) Color(0xFF10B981) else Color.Gray,
                    fontSize = 13.sp
                )
            }

            // Chat button
            IconButton(
                onClick = onChat,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(InteresadoGradientStart.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Chatear",
                    tint = InteresadoGradientStart,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
