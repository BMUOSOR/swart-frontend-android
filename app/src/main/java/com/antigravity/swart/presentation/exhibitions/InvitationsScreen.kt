package com.antigravity.swart.presentation.exhibitions

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

private val InvNavyBg     = Color(0xFF0B0D17)
private val InvCardBg     = Color(0xFF161925)
private val InvInputBg    = Color(0xFF1E2235)
private val InvNeonPink   = Color(0xFFFF2D87)
private val InvNeonPurple = Color(0xFF7B2FFF)
private val InvTextGray   = Color(0xFF8B8FA8)
private val InvTextLight  = Color(0xFFE8E8F0)

@Composable
fun InvitationsScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToChat: (Long) -> Unit = {},
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val invitations by viewModel.invitations.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        containerColor = InvNavyBg,
        bottomBar = {
            SwartBottomNav(
                userType = UserType.ARTIST,
                currentRoute = "mensajes",
                onNavigate = { route ->
                    when (route) {
                        "home"      -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa"      -> onNavigateToMap()
                        "perfil"    -> onNavigateToProfile()
                        "obras"     -> onNavigateToObras()
                    }
                },
                onFabClick = onNavigateToCreate
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── HEADER ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp).background(InvCardBg, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = InvTextLight)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Invitaciones",
                    color = InvTextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(42.dp))
        }

        // ─── TABS ─────────────────────────────────────────────────────
        val tabs = listOf("Chats", "Invitaciones")
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = InvCardBg,
            contentColor = InvNeonPurple,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = InvNeonPurple
                )
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == index) InvTextLight else InvTextGray
                        )
                    }
                )
            }
        }

        // ─── CONTENT ──────────────────────────────────────────────────
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = InvNeonPurple)
            }
        } else {
            if (selectedTabIndex == 0) {
                // Chats tab
                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = InvTextGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "No tienes conversaciones activas",
                                color = InvTextGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    conversations.forEach { conversation ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = InvCardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToChat(conversation.idConversacion) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(InvInputBg)
                                ) {
                                    if (!conversation.otherUserAvatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = conversation.otherUserAvatar,
                                            contentDescription = conversation.otherUserNombre,
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

                                // Details
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
                                            text = conversation.otherUserNombre,
                                            color = InvTextLight,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formatTime(conversation.fechaUltimoMensaje),
                                            color = InvTextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = conversation.ultimoMensaje,
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
            } else {
                // Invitaciones tab
                if (invitations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.MailOutline,
                                contentDescription = null,
                                tint = InvTextGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "No tienes invitaciones pendientes",
                                color = InvTextGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    invitations.forEach { invitation ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = InvCardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Expo thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(InvInputBg)
                                ) {
                                    if (invitation.exposicionImgUrl != null) {
                                        AsyncImage(
                                            model = invitation.exposicionImgUrl,
                                            contentDescription = invitation.tituloExposicion,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            tint = InvTextGray,
                                            modifier = Modifier.size(32.dp).align(Alignment.Center)
                                        )
                                    }
                                }

                                // Info
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Sender avatar
                                        AsyncImage(
                                            model = invitation.avatarArtistaSender,
                                            contentDescription = invitation.nombreArtistaSender,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(20.dp).clip(CircleShape).background(InvInputBg)
                                        )
                                        Text(
                                            invitation.nombreArtistaSender,
                                            color = InvNeonPurple,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        "te invita a colaborar en",
                                        color = InvTextGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "\"${invitation.tituloExposicion}\"",
                                        color = InvTextLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2
                                    )
                                }

                                // Botones aceptar / rechazar
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .clickable { viewModel.respondInvitation(invitation.idInvitacion, true) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .clickable { viewModel.respondInvitation(invitation.idInvitacion, false) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            Spacer(Modifier.height(16.dp))
        }
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
