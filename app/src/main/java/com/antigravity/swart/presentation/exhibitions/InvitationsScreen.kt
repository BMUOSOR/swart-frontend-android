package com.antigravity.swart.presentation.exhibitions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.data.remote.dto.InvitationDto
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.SwartLoadingIndicator
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.map.createMarkerBitmap
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
                                onRespond = { id, accept -> viewModel.respondInvitation(id, accept, "propuesta") },
                                onChatWithSender = { senderUserId ->
                                    viewModel.startChatWithSender(senderUserId) { chatId ->
                                        onNavigateToChat(chatId)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
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
private fun SpacesTab(
    propuestas: List<InvitationDto>,
    onRespond: (Long, Boolean) -> Unit,
    onChatWithSender: (Long) -> Unit
) {
    var selectedPropuesta by remember { mutableStateOf<InvitationDto?>(null) }

    if (propuestas.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Place,
            message = "No tienes propuestas de espacios",
            accent = InvTextGray
        )
        return
    }

    propuestas.forEach { propuesta ->
        SpaceRequestCard(
            propuesta = propuesta,
            onAccept = { onRespond(propuesta.idInvitacion, true) },
            onReject = { onRespond(propuesta.idInvitacion, false) },
            onClick = { selectedPropuesta = propuesta }
        )
    }

    selectedPropuesta?.let { p ->
        ProposalDetailSheet(
            propuesta = p,
            onAccept = {
                onRespond(p.idInvitacion, true)
                selectedPropuesta = null
            },
            onReject = {
                onRespond(p.idInvitacion, false)
                selectedPropuesta = null
            },
            onChat = {
                val userId = p.idUsuarioSender ?: p.idArtistaSender
                onChatWithSender(userId)
                selectedPropuesta = null
            },
            onDismiss = { selectedPropuesta = null }
        )
    }
}

/**
 * Tarjeta para la pestaña Invitaciones (sin click global, sólo aceptar/rechazar).
 */
@Composable
private fun RequestCard(
    thumbnailUrl: String?,
    senderAvatarUrl: String?,
    senderName: String,
    captionLine: String,
    title: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
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
                            thumbnailUrl.split(",").firstOrNull()?.trim()?.ifBlank { null } ?: "Pintura"
                        }
                        val bitmap = remember(firstTag) { createMarkerBitmap(context, firstTag, 1f) }
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = firstTag, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Icon(Icons.Default.Image, null, tint = InvTextGray, modifier = Modifier.size(32.dp).align(Alignment.Center))
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(model = senderAvatarUrl, contentDescription = senderName, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(InvInputBg))
                    Text(senderName, color = InvNeonPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Text(captionLine, color = InvTextGray, fontSize = 12.sp)
                Text("\"$title\"", color = InvTextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundActionButton(Icons.Default.Check, "Aceptar", InvSuccess, onAccept)
                RoundActionButton(Icons.Default.Close, "Rechazar", InvDanger, onReject)
            }
        }
    }
}

/**
 * Tarjeta para la pestaña Espacios: toda la tarjeta es clicable (abre el detalle)
 * y además tiene botones de Aceptar y Rechazar propios.
 */
@Composable
private fun SpaceRequestCard(
    propuesta: InvitationDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val firstTag = remember(propuesta.exposicionImgUrl) {
        propuesta.exposicionImgUrl
            ?.split(",")?.firstOrNull()?.trim()?.ifBlank { null } ?: "Pintura"
    }
    val bitmap = remember(firstTag) { createMarkerBitmap(context, firstTag, 1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp),
                ambientColor = InvNeonPurple.copy(alpha = 0.15f),
                spotColor = InvNeonPurple.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(18.dp))
            .background(InvCardBg)
            .border(1.dp, InvBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniatura — icono de categoría
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InvInputBg)
            ) {
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = firstTag, modifier = Modifier.fillMaxSize())
                // Indicador "Ver detalle" en esquina
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp))
                        .background(InvNeonPurple.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(model = propuesta.avatarArtistaSender, contentDescription = propuesta.nombreArtistaSender,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(InvInputBg))
                    Text(propuesta.nombreArtistaSender, color = InvNeonPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Text("ha enviado una propuesta:", color = InvTextGray, fontSize = 12.sp)
                Text("\"${propuesta.tituloExposicion}\"", color = InvTextLight, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2)
                // Chips de categoría y fechas
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!propuesta.categoria.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = InvNeonPurple.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InvNeonPurple.copy(alpha = 0.3f))
                        ) {
                            Text(propuesta.categoria, color = InvNeonPurple, fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    if (!propuesta.precio.toString().isBlank() && propuesta.precio != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = InvSuccess.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InvSuccess.copy(alpha = 0.25f))
                        ) {
                            Text("${propuesta.precio.toInt()}€", color = InvSuccess, fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundActionButton(Icons.Default.Check, "Aceptar", InvSuccess) { onAccept() }
                RoundActionButton(Icons.Default.Close, "Rechazar", InvDanger) { onReject() }
            }
        }
    }
}

/**
 * Sheet de detalle completo de una propuesta de espacio.
 * Muestra todos los campos que el artista rellenó + botones Chat / Aceptar / Rechazar.
 */
@Composable
private fun ProposalDetailSheet(
    propuesta: InvitationDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val firstTag = remember(propuesta.exposicionImgUrl) {
        propuesta.exposicionImgUrl
            ?.split(",")?.firstOrNull()?.trim()?.ifBlank { null } ?: "Pintura"
    }
    val bitmap = remember(firstTag) { createMarkerBitmap(context, firstTag, 1f) }
    val gradient = Brush.linearGradient(listOf(InvNeonPink, Color(0xFF8B5CF6)))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = InvNavyBg,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, InvBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Cabecera: avatar + nombre + cerrar ─────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(2.dp, gradient, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(InvInputBg)
                        ) {
                            AsyncImage(model = propuesta.avatarArtistaSender,
                                contentDescription = propuesta.nombreArtistaSender,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape))
                        }
                        Column {
                            Text(propuesta.nombreArtistaSender, color = InvTextLight,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (!propuesta.nombreEspacio.isNullOrBlank()) {
                                Text(
                                    "Espacio: ${propuesta.nombreEspacio}",
                                    color = InvNeonPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text("Propuesta de exposición", color = InvTextGray, fontSize = 12.sp)
                            }
                        }
                    }
                    IconButton(onClick = onDismiss,
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(InvInputBg)
                    ) {
                        Icon(Icons.Default.Close, "Cerrar", tint = InvTextGray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Icono de baliza grande ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(InvInputBg)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = firstTag,
                        modifier = Modifier.fillMaxSize())
                }

                Spacer(Modifier.height(16.dp))

                // ── Título ─────────────────────────────────────────────────────
                Text(
                    text = propuesta.tituloExposicion,
                    color = InvTextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                // ── Chip de categoría ──────────────────────────────────────────
                if (!propuesta.categoria.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = InvNeonPurple.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InvNeonPurple.copy(alpha = 0.3f))
                        ) {
                            Text(propuesta.categoria, color = InvNeonPurple,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Espacio disponible ────────────────────────────────────────
                if (!propuesta.nombreEspacio.isNullOrBlank()) {
                    DetailLabel("ESPACIO DISPONIBLE")
                    Spacer(Modifier.height(8.dp))
                    DetailInfoChip(
                        icon = Icons.Default.Place,
                        label = "Espacio",
                        value = propuesta.nombreEspacio,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }

                HorizontalDivider(color = InvBorder)
                Spacer(Modifier.height(20.dp))

                // ── Descripción ────────────────────────────────────────────────
                if (!propuesta.descrip.isNullOrBlank()) {
                    DetailLabel("DESCRIPCIÓN")
                    Spacer(Modifier.height(6.dp))
                    Text(propuesta.descrip, color = InvTextLight, fontSize = 14.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(16.dp))
                }

                // ── Fechas ─────────────────────────────────────────────────────
                if (!propuesta.fechaInicio.isNullOrBlank() || !propuesta.fechaFin.isNullOrBlank()) {
                    DetailLabel("FECHAS")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!propuesta.fechaInicio.isNullOrBlank()) {
                            DetailInfoChip(
                                icon = Icons.Default.CalendarToday,
                                label = "Inicio",
                                value = propuesta.fechaInicio,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!propuesta.fechaFin.isNullOrBlank()) {
                            DetailInfoChip(
                                icon = Icons.Default.EventAvailable,
                                label = "Fin",
                                value = propuesta.fechaFin,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Precio ─────────────────────────────────────────────────────
                if (propuesta.precio != null) {
                    DetailLabel("PRECIO SOLICITADO")
                    Spacer(Modifier.height(8.dp))
                    DetailInfoChip(
                        icon = Icons.Default.Euro,
                        label = "Precio",
                        value = "${propuesta.precio.toInt()} €",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── PDF adjunto ────────────────────────────────────────────
                if (!propuesta.archivoPdf.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    DetailLabel("PROPUESTA ADJUNTA")
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InvInputBg)
                            .border(1.dp, InvNeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse(propuesta.archivoPdf)
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, null, tint = InvNeonPurple, modifier = Modifier.size(18.dp))
                            Text("Ver propuesta en PDF", color = InvNeonPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = InvBorder)
                Spacer(Modifier.height(20.dp))

                // ── Acciones ───────────────────────────────────────────────────
                // Chat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6))))
                        .clickable { onChat() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Hablar con el artista", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    // Rechazar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, InvDanger.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .background(InvDanger.copy(alpha = 0.10f))
                            .clickable { onReject() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, null, tint = InvDanger, modifier = Modifier.size(16.dp))
                            Text("Rechazar", color = InvDanger, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    // Aceptar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(InvNeonPink, Color(0xFF8B5CF6))))
                            .clickable { onAccept() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Aceptar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(text, color = InvTextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
}

@Composable
private fun DetailInfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = InvInputBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, InvBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = InvNeonPurple, modifier = Modifier.size(16.dp))
            Column {
                Text(label, color = InvTextGray, fontSize = 10.sp)
                Text(value, color = InvTextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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