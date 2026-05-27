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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType

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
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val invitations by viewModel.invitations.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = InvNavyBg,
        bottomBar = {
            SwartBottomNav(
                userType = UserType.ARTIST,
                currentRoute = "perfil",
                onNavigate = { route ->
                    when (route) {
                        "home"      -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa"      -> onNavigateToMap()
                        "perfil"    -> onNavigateToProfile()
                        "obras"     -> onNavigateToObras()
                    }
                }
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
                Spacer(Modifier.size(42.dp))
            }

            // ─── CONTENT ──────────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = InvNeonPurple)
                }
            } else if (invitations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
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

            Spacer(Modifier.height(16.dp))
        }
    }
}
