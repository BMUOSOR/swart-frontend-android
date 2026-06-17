package com.antigravity.swart.presentation.exhibitions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import com.antigravity.swart.presentation.map.createMarkerBitmap
import com.antigravity.swart.data.remote.dto.InvitationDto
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.SwartLoadingIndicator
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

private val InvNavyBg     = Color(0xFF0B0D17)
private val InvCardBg     = Color(0xFF161925)
private val InvInputBg    = Color(0xFF1E2235)
private val InvBorder     = Color(0xFF272C42)
private val InvNeonPink   = Color(0xFFFF2D87)
private val InvNeonPurple = Color(0xFFEC4899)
private val InvTextGray   = Color(0xFF8B8FA8)
private val InvTextLight  = Color(0xFFE8E8F0)
private val InvSuccess    = Color(0xFF10B981)
private val InvDanger     = Color(0xFFEF4444)

@Composable
fun InvitationsScreen(
    role: String = "artista",
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val isArtista = role == "artista"
    val invitations by viewModel.invitations.collectAsState()
    val propuestasEspacios by viewModel.propuestasEspacios.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedProposalForDetail by remember { mutableStateOf<InvitationDto?>(null) }

    val tabAccent = if (isArtista) InvNeonPurple else InteresadoGradientStart
    val tabAccentGradient = Brush.horizontalGradient(listOf(InvNeonPink, tabAccent))

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = InvNavyBg,
        bottomBar = {
            SwartBottomNav(
                userType = if (isArtista) UserType.ARTIST else UserType.GENERAL,
                currentRoute = "mensajes",
                badges = mapOf("mensajes" to unreadCount),
                onNavigate = { route ->
                    when (route) {
                        "home"      -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa"      -> onNavigateToMap()
                        "perfil"    -> onNavigateToProfile()
                        "obras"     -> onNavigateToObras()
                        "favoritos" -> onNavigateToFavoritos()
                    }
                },
                onFabClick = onNavigateToCreate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Glow ambiental superior, mismo lenguaje visual que el resto de la app
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-90).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(tabAccent.copy(alpha = 0.22f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InvitationsHeader(title = "Mensajes", onBack = onBack)

                val tabs = if (isArtista) listOf("Chats", "Invitaciones", "Espacios") else listOf("Chats")
                InvitationsTabBar(
                    tabs = tabs,
                    selectedIndex = selectedTabIndex,
                    accentGradient = tabAccentGradient,
                    onSelect = { selectedTabIndex = it }
                )

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabContent"
                ) { tabIndex ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when {
                            isLoading -> LoadingState()
                            tabIndex == 0 -> ChatsTab(
                                conversations = conversations,
                                idOf = { it.idConversacion },
                                avatarOf = { it.otherUserAvatar },
                                nameOf = { it.otherUserNombre },
                                lastMessageOf = { it.ultimoMensaje },
                                lastMessageDateOf = { it.fechaUltimoMensaje },
                                onNavigateToChat = onNavigateToChat
                            )
                            isArtista && tabIndex == 1 -> InvitationsTab(
                                invitations = invitations,
                                idOf = { it.idInvitacion },
                                thumbnailOf = { it.exposicionImgUrl },
                                senderAvatarOf = { it.avatarArtistaSender },
                                senderNameOf = { it.nombreArtistaSender },
                                titleOf = { it.tituloExposicion },
                                onRespond = { id, accept -> viewModel.respondInvitation(id, accept, "invitacion") }
                            )
                            isArtista && tabIndex == 2 -> SpacesTab(
                                propuestas = propuestasEspacios,
                                idOf = { it.idInvitacion },
                                thumbnailOf = { it.exposicionImgUrl },
                                senderAvatarOf = { it.avatarArtistaSender },
                                senderNameOf = { it.nombreArtistaSender },
                                titleOf = { it.tituloExposicion },
                                onRespond = { id, accept -> viewModel.respondInvitation(id, accept, "propuesta") },
                                onProposalClick = { selectedProposalForDetail = it }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            selectedProposalForDetail?.let { proposal ->
                ProposalDetailDialog(
                    proposal = proposal,
                    onDismiss = { selectedProposalForDetail = null }
                )
            }
        }
    }
}

@Composable
private fun InvitationsHeader(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = title,
            color = InvTextLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(42.dp)
                .clip(CircleShape)
                .background(InvCardBg)
                .border(1.dp, InvBorder, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = InvTextLight)
        }
    }
}

@Composable
private fun InvitationsTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    accentGradient: Brush,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InvCardBg)
            .border(1.dp, InvBorder, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.background(accentGradient) else Modifier
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) InvTextLight else InvTextGray
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        SwartLoadingIndicator()
    }
}

@Composable
private fun EmptyState(icon: ImageVector, message: String, accent: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
            }
            Text(
                text = message,
                color = InvTextGray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun <T> ChatsTab(
    conversations: List<T>,
    idOf: (T) -> Long,
    avatarOf: (T) -> String?,
    nameOf: (T) -> String,
    lastMessageOf: (T) -> String,
    lastMessageDateOf: (T) -> String,
    onNavigateToChat: (Long) -> Unit
) {
    if (conversations.isEmpty()) {
        EmptyState(
            icon = Icons.Default.ChatBubbleOutline,
            message = "No tienes conversaciones activas",
            accent = InvTextGray
        )
        return
    }
    conversations.forEach { conversation ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(InvCardBg)
                .border(1.dp, InvBorder, RoundedCornerShape(18.dp))
                .clickable { onNavigateToChat(idOf(conversation)) }
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(InvInputBg)
                ) {
                    val avatar = avatarOf(conversation)
                    if (!avatar.isNullOrBlank()) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = nameOf(conversation),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = InvTextGray,
                            modifier = Modifier.size(32.dp).align(Alignment.Center)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nameOf(conversation),
                            color = InvTextLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatTime(lastMessageDateOf(conversation)),
                            color = InvTextGray,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = lastMessageOf(conversation),
                        color = InvTextGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> InvitationsTab(
    invitations: List<T>,
    idOf: (T) -> Long,
    thumbnailOf: (T) -> String?,
    senderAvatarOf: (T) -> String?,
    senderNameOf: (T) -> String,
    titleOf: (T) -> String,
    onRespond: (Long, Boolean) -> Unit
) {
    if (invitations.isEmpty()) {
        EmptyState(
            icon = Icons.Default.MailOutline,
            message = "No tienes invitaciones pendientes",
            accent = InvTextGray
        )
        return
    }
    invitations.forEach { invitation ->
        RequestCard(
            thumbnailUrl = thumbnailOf(invitation),
            senderAvatarUrl = senderAvatarOf(invitation),
            senderName = senderNameOf(invitation),
            captionLine = "te invita a colaborar en",
            title = titleOf(invitation),
            onAccept = { onRespond(idOf(invitation), true) },
            onReject = { onRespond(idOf(invitation), false) }
        )
    }
}

@Composable
private fun <T> SpacesTab(
    propuestas: List<T>,
    idOf: (T) -> Long,
    thumbnailOf: (T) -> String?,
    senderAvatarOf: (T) -> String?,
    senderNameOf: (T) -> String,
    titleOf: (T) -> String,
    onRespond: (Long, Boolean) -> Unit,
    onProposalClick: (T) -> Unit
) {
    if (propuestas.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Place,
            message = "No tienes propuestas de espacios",
            accent = InvTextGray
        )
        return
    }
    propuestas.forEach { propuesta ->
        RequestCard(
            thumbnailUrl = thumbnailOf(propuesta),
            senderAvatarUrl = senderAvatarOf(propuesta),
            senderName = senderNameOf(propuesta),
            captionLine = "ha enviado una propuesta:",
            title = titleOf(propuesta),
            onAccept = { onRespond(idOf(propuesta), true) },
            onReject = { onRespond(idOf(propuesta), false) },
            onThumbnailClick = { onProposalClick(propuesta) }
        )
    }
}

/**
 * Tarjeta compartida por la pestaña de Invitaciones y la de Espacios:
 * miniatura, remitente, descripción y acciones de aceptar/rechazar.
 */
@Composable
private fun RequestCard(
    thumbnailUrl: String?,
    senderAvatarUrl: String?,
    senderName: String,
    captionLine: String,
    title: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onThumbnailClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = InvNeonPurple.copy(alpha = 0.15f),
                spotColor = InvNeonPurple.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(InvCardBg)
            .border(1.dp, InvBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InvInputBg)
                    .then(
                        if (onThumbnailClick != null) Modifier.clickable { onThumbnailClick() }
                        else Modifier
                    )
            ) {
                if (thumbnailUrl != null) {
                    if (thumbnailUrl.startsWith("http")) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val firstTag = remember(thumbnailUrl) {
                            val tag = thumbnailUrl.split(",").firstOrNull()?.trim()
                            if (tag.isNullOrBlank()) "Pintura" else tag
                        }
                        val bitmap = remember(firstTag) {
                            createMarkerBitmap(context, firstTag, 1f)
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = thumbnailUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = InvTextGray,
                        modifier = Modifier.size(32.dp).align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(
                        model = senderAvatarUrl,
                        contentDescription = senderName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(InvInputBg)
                    )
                    Text(
                        senderName,
                        color = InvNeonPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Text(captionLine, color = InvTextGray, fontSize = 12.sp)
                Text(
                    "\"$title\"",
                    color = InvTextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RoundActionButton(
                    icon = Icons.Default.Check,
                    contentDescription = "Aceptar",
                    color = InvSuccess,
                    onClick = onAccept
                )
                RoundActionButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Rechazar",
                    color = InvDanger,
                    onClick = onReject
                )
            }
        }
    }
}

@Composable
private fun RoundActionButton(
    icon: ImageVector,
    contentDescription: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(20.dp))
    }
}

fun formatTime(dateTimeStr: String): String {
    return try {
        val parts = dateTimeStr.split(" ")
        if (parts.size >= 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            if (timeParts.size >= 2) {
                "${timeParts[0]}:${timeParts[1]}"
            } else parts[1]
        } else {
            val partsDash = dateTimeStr.split("-")
            if (partsDash.size == 3) {
                "${partsDash[2]}/${partsDash[1]}"
            } else dateTimeStr
        }
    } catch (e: Exception) {
        dateTimeStr
    }
}

@Composable
fun ProposalDetailDialog(
    proposal: InvitationDto,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = InvNeonPurple)
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "Detalle de Propuesta",
                color = InvTextLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Título de la exposición", color = InvTextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(proposal.tituloExposicion, color = InvTextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = proposal.avatarArtistaSender,
                        contentDescription = proposal.nombreArtistaSender,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(InvInputBg)
                    )
                    Column {
                        Text("Artista", color = InvTextGray, fontSize = 11.sp)
                        Text(proposal.nombreArtistaSender, color = InvNeonPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val firstTag = remember(proposal.exposicionImgUrl) {
                        val tag = proposal.exposicionImgUrl?.split(",")?.firstOrNull()?.trim()
                        if (tag.isNullOrBlank()) "Pintura" else tag
                    }
                    val bitmap = remember(firstTag) {
                        createMarkerBitmap(context, firstTag, 1f)
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InvInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = firstTag,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Column {
                        Text("Categoría", color = InvTextGray, fontSize = 11.sp)
                        Text(firstTag, color = InvTextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fechas", color = InvTextGray, fontSize = 11.sp)
                        val start = proposal.fechaInicio ?: "N/D"
                        val end = proposal.fechaFin ?: "N/D"
                        Text("$start al $end", color = InvTextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Precio", color = InvTextGray, fontSize = 11.sp)
                        val priceText = if (proposal.precio == null || proposal.precio == 0.0) "Gratis" else "${proposal.precio} €"
                        Text(priceText, color = InvSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Descripción", color = InvTextGray, fontSize = 11.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InvInputBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = proposal.descrip ?: "Sin descripción.",
                            color = InvTextLight,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        containerColor = InvCardBg,
        textContentColor = InvTextLight,
        titleContentColor = InvTextLight,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, InvBorder, RoundedCornerShape(24.dp))
    )
}