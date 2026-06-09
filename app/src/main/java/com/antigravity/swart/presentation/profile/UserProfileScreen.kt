package com.antigravity.swart.presentation.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.yalantis.ucrop.UCrop
import java.io.File
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    role: String = "interesado",
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToObras: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToArtistas: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToArtistProfile: (Long) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Siempre leer el rol desde SessionManager — el parámetro de nav puede quedar
    // obsoleto al volver de sub-pantallas como ArtistasListScreen/ArtistProfileScreen
    val sessionManager = remember { com.antigravity.swart.core.SessionManager(context) }
    val resolvedRole = remember { sessionManager.getRole() ?: "interesado" }

    // Step 2: receive cropped result from uCrop → upload
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val croppedUri = result.data?.let { UCrop.getOutput(it) }
            croppedUri?.let { viewModel.uploadAndUpdateAvatar(it, context) }
        }
    }

    // Step 1: pick image from gallery → launch uCrop
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { sourceUri ->
            val destFile = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.jpg")
            val destUri = android.net.Uri.fromFile(destFile)
            val uCropIntent = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1f, 1f)          // Square → circle in UI
                .withMaxResultSize(512, 512)
                .withOptions(UCrop.Options().apply {
                    setToolbarTitle("Recortar foto de perfil")
                    setToolbarColor(0xFF0B0D17.toInt())
                    setStatusBarColor(0xFF0B0D17.toInt())
                    setToolbarWidgetColor(android.graphics.Color.WHITE)
                    setActiveControlsWidgetColor(0xFFEC4899.toInt())
                    setCropFrameColor(0xFFEC4899.toInt())
                    setCropGridColor(0x33FFFFFF)
                    setCircleDimmedLayer(true)     // circular overlay preview
                    setShowCropGrid(false)
                    setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    setCompressionQuality(92)
                    setHideBottomControls(false)
                })
                .getIntent(context)
            uCropLauncher.launch(uCropIntent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Show snackbar when upload fails
    LaunchedEffect(uiState.uploadError) {
        val err = uiState.uploadError
        if (!err.isNullOrBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(err)
                viewModel.clearUploadError()
            }
        }
    }

    val scrollState = rememberScrollState()

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
    val initial = userName.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = Color(0xFF0B0D17),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val resolvedUserType = if (resolvedRole == "artista") UserType.ARTIST else UserType.GENERAL
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
                        "favoritos" -> onNavigateToFavorites()
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

            // 1. Cabecera de Perfil
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Avatar container with edit button
                Box(modifier = Modifier.size(130.dp)) {
                    if (avatarUrl.isBlank()) {
                        // Placeholder: initial letter on gradient background
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                    )
                                )
                                .border(4.dp, neonPinkToPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .memoryCacheKey(avatarUrl)
                                .diskCacheKey(avatarUrl)
                                .build(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .border(4.dp, neonPinkToPurple, CircleShape)
                        )
                    }

                    // Botón lápiz — editar foto
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF161925))
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .clickable(enabled = !uiState.isUploadingAvatar) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isUploadingAvatar) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Ubicación",
                        tint = Color(0xFF8B5CF6),
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
                            text = if (resolvedRole == "artista")
                                "Responde a tus seguidores e interesados"
                            else
                                "Chatea con tus artistas favoritos",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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

            // 3. Sección de Ajustes
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AJUSTES",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161925)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (resolvedRole == "artista") {
                            SettingMenuItem(
                                icon = Icons.Filled.AccountCircle,
                                title = "Mi Perfil de Artista",
                                isLast = false,
                                onClick = {
                                    val sessionManager = com.antigravity.swart.core.SessionManager(context)
                                    val userId = sessionManager.getUserId()
                                    if (userId != -1L) onNavigateToArtistProfile(userId)
                                }
                            )
                            SettingMenuItem(
                                icon = Icons.Filled.Brush,
                                title = "Mis Obras",
                                isLast = false,
                                onClick = onNavigateToObras
                            )
                            SettingMenuItem(
                                icon = Icons.Filled.Notifications,
                                title = "Notificaciones",
                                isLast = true,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        } else {
                            SettingMenuItem(
                                icon = Icons.Filled.Favorite,
                                title = "Mis Favoritos",
                                isLast = false,
                                onClick = onNavigateToFavorites
                            )
                            SettingMenuItem(
                                icon = Icons.Filled.People,
                                title = "Artistas Seguidos",
                                isLast = false,
                                onClick = onNavigateToArtistas
                            )
                            SettingMenuItem(
                                icon = Icons.Filled.Notifications,
                                title = "Notificaciones",
                                isLast = true,
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
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

            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

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
