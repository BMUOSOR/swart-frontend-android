package com.antigravity.swart.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.*

@Composable
fun UserProfileScreen(
    role: String = "interesado",
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToObras: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val scrollState = rememberScrollState()
    
    // Gradient definitions
    val neonPinkToPurple = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    )
    val purpleToNeonBlue = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
    )
    val logoutGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48))
    )

    val avatarUrl = uiState.imgUrl
    val userName = "${uiState.nombre} ${uiState.apellidos}".trim().ifBlank { "Usuario Swart" }
    val userLocation = uiState.location

    Scaffold(
        containerColor = Color(0xFF0B0D17), // Deep dark navy blue background
        bottomBar = {
            val sessionManager = androidx.compose.runtime.remember { com.antigravity.swart.core.SessionManager(context) }
            val resolvedUserType = if (sessionManager.getRole() == "artista") UserType.ARTIST else UserType.GENERAL
            SwartBottomNav(
                userType = resolvedUserType,
                currentRoute = "perfil",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa" -> onNavigateToMap()
                        "obras" -> onNavigateToObras()
                        "mensajes" -> onNavigateToMensajes()
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // 1. Cabecera de Perfil (Header)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Avatar container with share button
                Box(
                    modifier = Modifier.size(130.dp)
                ) {
                    // Profile Image with neon gradient border
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .border(4.dp, neonPinkToPurple, CircleShape)
                    )
                    
                    // Share Button overlaid on bottom right
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF161925)) // Dark background
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .clickable { /* Share Profile */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Compartir",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Name
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Ubicación",
                        tint = Color(0xFF8B5CF6), // Purple pin icon
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = userLocation,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            
            // 2. Tarjeta de Conversaciones
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMensajes() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Gradient Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(purpleToNeonBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubble,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Center Titles
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Conversaciones",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chatea con tus artistas favoritos",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Right red badge + chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge
                        if (uiState.pendingInvitationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.pendingInvitationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Ver conversaciones",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // 3. Sección de Ajustes (Stacked Menu)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section Title
                Text(
                    text = "AJUSTES",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                // Stacked menu block card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingMenuItem(
                            icon = Icons.Filled.AccountCircle,
                            title = "Mi Cuenta",
                            isLast = false,
                            onClick = {
                                val sessionManager = com.antigravity.swart.core.SessionManager(context)
                                val userId = sessionManager.getUserId()
                                if (userId != -1L) {
                                    onNavigateToArtistProfile(userId)
                                }
                            }
                        )
                        SettingMenuItem(
                            icon = Icons.Filled.Brush,
                            title = "Preferencias de Arte",
                            isLast = false,
                            onClick = {}
                        )
                        SettingMenuItem(
                            icon = Icons.Filled.Favorite,
                            title = "Artistas Seguidos",
                            isLast = false,
                            onClick = onNavigateToFavorites
                        )
                        SettingMenuItem(
                            icon = Icons.Filled.Notifications,
                            title = "Notificaciones",
                            isLast = false,
                            onClick = {}
                        )
                        SettingMenuItem(
                            icon = Icons.Filled.Lock,
                            title = "Privacidad",
                            isLast = true,
                            onClick = {}
                        )
                    }
                }
            }
            
            // 4. Botón de Cerrar Sesión
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(logoutGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                        Text(
                            text = "Cerrar Sesión",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingMenuItem(
    icon: ImageVector,
    title: String,
    isLast: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Circle Gray Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF242838)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Menu Title Text
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            // Right Chevron
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        if (!isLast) {
            Divider(
                color = Color(0xFF242838),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
