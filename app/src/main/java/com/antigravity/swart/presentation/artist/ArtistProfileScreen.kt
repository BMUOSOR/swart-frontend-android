package com.antigravity.swart.presentation.artist

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.model.ArtworkForSale
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.presentation.components.SwartLoadingIndicator
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Colores premium del tema
val NavyBackground = Color(0xFF0B0D17)
val NavyCardBackground = Color(0xFF161925)
val PremiumPink = Color(0xFFEC4899)
val PremiumPurple = Color(0xFF8B5CF6)
val GrayTextLight = Color(0xFFD1D5DB)
val GrayTextDark = Color(0xFF9CA3AF)

@Composable
fun ArtistProfileScreen(
    viewModel: ArtistProfileViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isSavingProfile by viewModel.isSavingProfile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NavyBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ArtistProfileUiState.Loading -> {
                    SwartLoadingIndicator()
                }
                is ArtistProfileUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error al cargar el perfil: ${state.message}", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadArtistProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumPink)
                        ) {
                            Text("Reintentar", color = Color.White)
                        }
                    }
                }
                is ArtistProfileUiState.Success -> {
                    var showInquiryDialog by remember { mutableStateOf(false) }
                    var selectedWorkForInquiry by remember { mutableStateOf<ArtworkForSale?>(null) }

                    ArtistProfileContent(
                        profile = state.profile,
                        isFollowing = isFollowing,
                        isOwnProfile = viewModel.isOwnProfile,
                        onBack = onBack,
                        onNavigateToDetail = onNavigateToDetail,
                        onFollowToggle = { viewModel.toggleFollow() },
                        onEditProfileClick = { showEditProfileDialog = true },
                        onInquiryClick = { work ->
                            selectedWorkForInquiry = work
                            showInquiryDialog = true
                        },
                        onChatClick = {
                            viewModel.startGeneralChat { chatId ->
                                onNavigateToChat(chatId)
                            }
                        }
                    )

                    // Diálogo de edición de perfil
                    if (showEditProfileDialog) {
                        var bioInput       by remember { mutableStateOf(state.profile.bio ?: "") }
                        var instagramInput by remember { mutableStateOf(state.profile.instagram ?: "") }
                        var twitterInput   by remember { mutableStateOf(state.profile.twitter ?: "") }
                        var correoInput    by remember { mutableStateOf(state.profile.correo ?: "") }

                        AlertDialog(
                            onDismissRequest = { if (!isSavingProfile) showEditProfileDialog = false },
                            containerColor = Color(0xFF161925),
                            title = {
                                Text("Editar perfil", color = Color.White, fontWeight = FontWeight.Bold)
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = bioInput,
                                        onValueChange = { bioInput = it },
                                        label = { Text("Biografía", color = Color(0xFF9CA3AF)) },
                                        minLines = 3,
                                        maxLines = 5,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = PremiumPink,
                                            unfocusedBorderColor = Color(0xFF374151)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = instagramInput,
                                        onValueChange = { instagramInput = it },
                                        label = { Text("Instagram (@usuario)", color = Color(0xFF9CA3AF)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = PremiumPink,
                                            unfocusedBorderColor = Color(0xFF374151)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = twitterInput,
                                        onValueChange = { twitterInput = it },
                                        label = { Text("Twitter / X (@usuario)", color = Color(0xFF9CA3AF)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = PremiumPink,
                                            unfocusedBorderColor = Color(0xFF374151)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = correoInput,
                                        onValueChange = { correoInput = it },
                                        label = { Text("Correo de contacto", color = Color(0xFF9CA3AF)) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = PremiumPink,
                                            unfocusedBorderColor = Color(0xFF374151)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.updateProfile(
                                            bio       = bioInput.ifBlank { null },
                                            instagram = instagramInput.ifBlank { null },
                                            twitter   = twitterInput.ifBlank { null },
                                            correo    = correoInput.ifBlank { null }
                                        )
                                        showEditProfileDialog = false
                                    },
                                    enabled = !isSavingProfile
                                ) {
                                    if (isSavingProfile) {
                                        CircularProgressIndicator(
                                            color = PremiumPink,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Guardar", color = PremiumPink, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditProfileDialog = false }) {
                                    Text("Cancelar", color = Color.Gray)
                                }
                            }
                        )
                    }

                    if (showInquiryDialog && selectedWorkForInquiry != null) {
                        val work = selectedWorkForInquiry!!
                        AlertDialog(
                            onDismissRequest = { showInquiryDialog = false },
                            containerColor = Color(0xFF161925),
                            title = {
                                Text(
                                    text = "Preguntar por obra",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    text = "¿Quieres preguntar al artista sobre la obra \"${work.titulo}\"?",
                                    color = Color(0xFF8B8FA8)
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showInquiryDialog = false
                                        viewModel.startInquiryChat(work) { chatId ->
                                            onNavigateToChat(chatId)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Sí",
                                        color = Color(0xFFEC4899),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showInquiryDialog = false }
                                ) {
                                    Text(
                                        text = "No",
                                        color = Color.Gray
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistProfileContent(
    profile: ArtistProfile,
    isFollowing: Boolean,
    isOwnProfile: Boolean,
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit = {},
    onFollowToggle: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onInquiryClick: (ArtworkForSale) -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val accentGradient = Brush.horizontalGradient(
        colors = listOf(PremiumPink, PremiumPurple)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // 1. Barra de navegación superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(NavyCardBackground, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        val nombreCompleto = "${profile.nombre} ${profile.apellidos ?: ""}".trim()
                        val sb = StringBuilder()
                        sb.appendLine("¡Mira este artista en SWART!")
                        sb.appendLine(nombreCompleto)
                        profile.bio?.let { sb.appendLine("\"$it\"") }
                        profile.instagram?.let { sb.appendLine("Instagram: @${it.trimStart('@')}") }
                        profile.twitter?.let { sb.appendLine("Twitter/X: @${it.trimStart('@')}") }
                        profile.correo?.let { sb.appendLine("Correo: $it") }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, sb.toString().trimEnd())
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir artista"))
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(NavyCardBackground, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.White
                    )
                }

                if (isOwnProfile) {
                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(PremiumPink, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(
                        onClick = onFollowToggle,
                        modifier = Modifier
                            .size(44.dp)
                            .background(NavyCardBackground, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFollowing) PremiumPink else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Hero Section del Artista
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .border(4.dp, accentGradient, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(profile.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre completo
            val nombreCompleto = "${profile.nombre} ${profile.apellidos ?: ""}".trim()
            Text(
                text = nombreCompleto,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Profesión en morado
            Text(
                text = "Artista Contemporáneo",
                color = PremiumPurple,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Biografía
            Text(
                text = profile.bio ?: "Sin biografía disponible por el momento.",
                color = GrayTextLight,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (!isOwnProfile) {
            // 3. Botón principal: Chatear con el artista
            Button(
                onClick = onChatClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(accentGradient)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chatear con el artista",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Estadísticas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SEGUIDORES",
                        color = GrayTextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.seguidores}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EXPOSICIONES",
                        color = GrayTextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.exposicionesCount}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Redes Sociales
        val hasAnySocial = profile.instagram != null || profile.twitter != null || profile.correo != null
        if (hasAnySocial) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                profile.instagram?.let { handle ->
                    val url = if (handle.startsWith("http")) handle
                              else "https://www.instagram.com/${handle.trimStart('@')}"
                    SocialMediaButton(
                        icon = Icons.Default.CameraAlt,
                        description = "Instagram",
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                profile.twitter?.let { handle ->
                    val url = if (handle.startsWith("http")) handle
                              else "https://x.com/${handle.trimStart('@')}"
                    SocialMediaButton(
                        icon = Icons.Default.AlternateEmail,
                        description = "Twitter / X",
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                profile.correo?.let { email ->
                    SocialMediaButton(
                        icon = Icons.Default.Email,
                        description = "Correo",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                            }
                            context.startActivity(Intent.createChooser(intent, "Enviar correo"))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Exposiciones activas
        val allExpos = profile.activeExhibitions
        if (allExpos.isNotEmpty()) {
            var showAllExpos by remember { mutableStateOf(false) }
            val visibleExpos = if (showAllExpos || allExpos.size <= 2) allExpos else allExpos.take(2)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exposiciones",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (allExpos.size > 2) {
                        Text(
                            text = if (showAllExpos) "Ver menos" else "Ver todas (${allExpos.size})",
                            color = PremiumPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showAllExpos = !showAllExpos }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                visibleExpos.forEach { exhibition ->
                    ExhibitionCardSpanish(
                        exhibition = exhibition,
                        context = context,
                        isOwnProfile = isOwnProfile,
                        onClick = { onNavigateToDetail(exhibition.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Obras en venta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Obras en venta",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (profile.worksForSale.isNotEmpty()) {
                // Agrupamos en filas de a 2 obras para recrear el Grid
                val chunkedWorks = profile.worksForSale.chunked(2)
                chunkedWorks.forEach { rowWorks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowWorks.forEach { work ->
                            Box(modifier = Modifier.weight(1f)) {
                                WorkCardSpanish(
                                    work = work,
                                    context = context,
                                    isOwnProfile = isOwnProfile,
                                    onInquiryClick = onInquiryClick
                                )
                            }
                        }
                        if (rowWorks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Text(
                    text = "El artista no tiene obras en venta en este momento.",
                    color = GrayTextDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SocialMediaButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(NavyCardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = PremiumPink,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ExhibitionCardSpanish(
    exhibition: Exhibition,
    context: android.content.Context,
    isOwnProfile: Boolean = false,
    onClick: () -> Unit = {}
) {
    val today = LocalDate.now()
    var badgeText: String? = null
    var badgeColor = Color(0xFFEF4444)

    val endDate = exhibition.fechaFin?.let {
        try { LocalDate.parse(it) } catch (e: Exception) { null }
    }
    val startDate = exhibition.fechaInicio?.let {
        try { LocalDate.parse(it) } catch (e: Exception) { null }
    }

    if (endDate != null) {
        val daysToClose = ChronoUnit.DAYS.between(today, endDate)
        if (daysToClose in 0L..3L) {
            badgeText = "CIERRA PRONTO"
            badgeColor = Color(0xFFEF4444)
        }
    }

    if (badgeText == null && startDate != null) {
        val daysSinceOpen = ChronoUnit.DAYS.between(startDate, today)
        if (daysSinceOpen in 0L..3L) {
            badgeText = "NUEVO"
            badgeColor = Color(0xFF10B981)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardBackground)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(exhibition.exhibitionImgUrl ?: (exhibition.artworks.firstOrNull()?.imageUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = exhibition.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(badgeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = exhibition.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha",
                        tint = GrayTextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${exhibition.fechaInicio ?: ""} - ${exhibition.fechaFin ?: ""}",
                        color = GrayTextDark,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Lugar",
                        tint = GrayTextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = exhibition.nombreLugar ?: "Lugar desconocido",
                        color = GrayTextDark,
                        fontSize = 13.sp
                    )
                }

                if (isOwnProfile) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExhibitionStatsRow(
                        visitantes = exhibition.visitantes,
                        favoritos = exhibition.favoritosCount,
                        obras = exhibition.artworksCount
                    )
                }
            }
        }
    }
}

@Composable
fun WorkCardSpanish(
    work: ArtworkForSale,
    context: android.content.Context,
    isOwnProfile: Boolean = false,
    onInquiryClick: (ArtworkForSale) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCardBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(work.imgUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = work.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = work.titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%,.0f", work.precio)}€",
                        color = PremiumPurple,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isOwnProfile) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .clickable { onInquiryClick(work) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExhibitionStatsRow(
    visitantes: Long,
    favoritos: Int,
    obras: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Visitantes
        StatItem(
            icon = Icons.Default.RemoveRedEye,
            label = "Visitas",
            value = visitantes.toString(),
            tint = PremiumPink
        )

        // Favoritos (likes en obras)
        StatItem(
            icon = Icons.Default.Favorite,
            label = "Favoritos",
            value = favoritos.toString(),
            tint = PremiumPink
        )

        // Obras
        StatItem(
            icon = Icons.Default.Brush,
            label = "Obras",
            value = obras.toString(),
            tint = PremiumPurple
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = GrayTextDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
