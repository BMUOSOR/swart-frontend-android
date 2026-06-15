package com.antigravity.swart.presentation.map

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ─── Local palette ────────────────────────────────────────────────────────────
private val BvNavy      = Color(0xFF0B0D17)
private val BvCard      = Color(0xFF161925)
private val BvInput     = Color(0xFF1E2235)
private val BvNeonPink  = Color(0xFFFF2D87)
private val BvNeonPurp  = Color(0xFFEC4899)
private val BvTextGray  = Color(0xFF8B8FA8)
private val BvTextLight = Color(0xFFE8E8F0)
private val BvGreen     = Color(0xFF10B981)

private val ALL_CATEGORIES = listOf(
    "Pintura", "Escultura", "Fotografía", "Arte Digital",
    "Instalación", "Performance", "Grabado", "Textil", "Mixta"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalizaVaciaDetailScreen(
    balizaId: Long,
    onBack: () -> Unit,
    onNavigateToProposal: () -> Unit = {},  // opens ProposalFormDialog, handled by MapScreen
    onNavigateToArtistProfile: (Long) -> Unit = {},
    viewModel: BalizaVaciaDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = BvNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BvNeonPurp)
                }
            }
            uiState.baliza == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Baliza no encontrada", color = BvTextGray)
                }
            }
            else -> {
                val baliza = uiState.baliza!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ─── HEADER / FOTO DEL ESPACIO ────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (!baliza.fotos.isNullOrBlank()) {
                            // Mostrar primera foto del espacio
                            val firstPhoto = try {
                                baliza.fotos.trim('[', ']').split(",")
                                    .firstOrNull()?.trim()?.trim('"') ?: ""
                            } catch (e: Exception) { "" }

                            if (firstPhoto.isNotBlank()) {
                                AsyncImage(
                                    model = firstPhoto,
                                    contentDescription = "Espacio",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                DefaultSpaceBackground()
                            }
                        } else {
                            DefaultSpaceBackground()
                        }

                        // Gradient overlay
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            BvNavy
                                        )
                                    )
                                )
                        )

                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .align(Alignment.TopStart)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Atrás", tint = BvTextLight)
                        }

                        // Propietario avatar (top right)
                        if (!baliza.avatarPropietario.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(BvInput)
                                    .border(2.dp, BvNeonPurp, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .clickable { onNavigateToArtistProfile(baliza.idPropietario) }
                            ) {
                                AsyncImage(
                                    model = baliza.avatarPropietario,
                                    contentDescription = baliza.nombrePropietario,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // "ESPACIO VACÍO" badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(BvNeonPink, BvNeonPurp)),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("📍 ESPACIO VACÍO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ─── CONTENT ───────────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Propietario info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BvInput)
                            ) {
                                if (!baliza.avatarPropietario.isNullOrBlank()) {
                                    AsyncImage(
                                        model = baliza.avatarPropietario,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.AccountCircle, null, tint = BvTextGray,
                                        modifier = Modifier.size(22.dp).align(Alignment.Center))
                                }
                            }
                            Text("Espacio de ${baliza.nombrePropietario}", color = BvTextGray, fontSize = 13.sp)
                        }

                        // ── TÍTULO ──────────────────────────────────────────
                        if (uiState.isPropietario) {
                            OutlinedTextField(
                                value = uiState.titulo,
                                onValueChange = viewModel::onTituloChange,
                                label = { Text("Título del espacio", color = BvTextGray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BvNeonPurp,
                                    unfocusedBorderColor = BvInput,
                                    focusedTextColor = BvTextLight,
                                    unfocusedTextColor = BvTextLight,
                                    cursorColor = BvNeonPurp
                                )
                            )
                        } else {
                            Text(
                                text = baliza.titulo?.ifBlank { "Espacio sin nombre" } ?: "Espacio sin nombre",
                                color = BvTextLight,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ── DESCRIPCIÓN ──────────────────────────────────────
                        SectionLabel("Descripción")
                        if (uiState.isPropietario) {
                            OutlinedTextField(
                                value = uiState.descripcion,
                                onValueChange = viewModel::onDescripcionChange,
                                label = { Text("Describe este espacio", color = BvTextGray) },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BvNeonPurp,
                                    unfocusedBorderColor = BvInput,
                                    focusedTextColor = BvTextLight,
                                    unfocusedTextColor = BvTextLight,
                                    cursorColor = BvNeonPurp
                                )
                            )
                        } else {
                            Text(
                                baliza.descripcion?.ifBlank { "Sin descripción" } ?: "Sin descripción",
                                color = BvTextGray,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }

                        // ── CATEGORÍAS ACEPTADAS ─────────────────────────────
                        SectionLabel("Tipos de exposición aceptados")
                        if (uiState.isPropietario) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(ALL_CATEGORIES) { cat ->
                                    val selected = uiState.categoriasList.contains(cat)
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.toggleCategoria(cat) },
                                        label = { Text(cat, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BvNeonPurp.copy(alpha = 0.25f),
                                            selectedLabelColor = BvNeonPurp,
                                            containerColor = BvInput,
                                            labelColor = BvTextGray
                                        )
                                    )
                                }
                            }
                        } else {
                            val cats = baliza.categorias?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
                            if (cats.isNullOrEmpty()) {
                                Text("Sin restricciones de categoría", color = BvTextGray, fontSize = 14.sp)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(cats) { cat ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = BvNeonPurp.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                cat,
                                                color = BvNeonPurp,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── DIMENSIONES ──────────────────────────────────────
                        SectionLabel("Dimensiones del espacio")
                        if (uiState.isPropietario) {
                            OutlinedTextField(
                                value = uiState.dimensiones,
                                onValueChange = viewModel::onDimensionesChange,
                                label = { Text("Ej: 150m², 3 plantas", color = BvTextGray) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Straighten, null, tint = BvTextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BvNeonPurp,
                                    unfocusedBorderColor = BvInput,
                                    focusedTextColor = BvTextLight,
                                    unfocusedTextColor = BvTextLight,
                                    cursorColor = BvNeonPurp
                                )
                            )
                        } else {
                            InfoRow(Icons.Default.Straighten, baliza.dimensiones ?: "No especificadas")
                        }

                        // ── SALAS ────────────────────────────────────────────
                        SectionLabel("Salas")
                        if (uiState.isPropietario) {
                            OutlinedTextField(
                                value = uiState.salas,
                                onValueChange = { viewModel.onDimensionesChange(it) },
                                label = { Text("Ej: [{\"nombre\":\"Sala A\",\"superficie\":\"80m²\"}]", color = BvTextGray) },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.MeetingRoom, null, tint = BvTextGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BvNeonPurp,
                                    unfocusedBorderColor = BvInput,
                                    focusedTextColor = BvTextLight,
                                    unfocusedTextColor = BvTextLight,
                                    cursorColor = BvNeonPurp
                                )
                            )
                        } else {
                            if (baliza.salas.isNullOrBlank()) {
                                InfoRow(Icons.Default.MeetingRoom, "Sin salas definidas")
                            } else {
                                // Parse simple: mostrar cantidad de salas
                                val salaCount = try {
                                    baliza.salas.count { it == '{' }
                                } catch (e: Exception) { 0 }
                                InfoRow(Icons.Default.MeetingRoom, "$salaCount sala(s) disponible(s)")
                            }
                        }

                        // ── SAVE BUTTON (solo propietario) ───────────────────
                        if (uiState.isPropietario) {
                            Button(
                                onClick = viewModel::saveChanges,
                                enabled = !uiState.isSaving,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(listOf(BvNeonPink, BvNeonPurp)),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Save, null, tint = Color.White)
                                            Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ── PROPONER EXPOSICIÓN (solo visitantes) ────────────
                        if (!uiState.isPropietario) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.toggleProposalForm(true) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(listOf(BvNeonPink, BvNeonPurp)),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Send, null, tint = Color.White)
                                        Text(
                                            "Proponer mi exposición aquí",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                        } else {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }

        if (uiState.isProposalFormOpen) {
            ProposalFormDialog(
                propietarioId = uiState.baliza?.idPropietario ?: -1L,
                currentUserAvatarUrl = viewModel.getCurrentUserAvatarUrl(),
                onSend = { t, d, s, e, c, p -> viewModel.sendProposal(t, d, s, e, c, p) },
                onNavigateToArtistProfile = onNavigateToArtistProfile,
                onDismiss = { viewModel.toggleProposalForm(false) }
            )
        }
    }
}

@Composable
private fun DefaultSpaceBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1E2235), Color(0xFF0B0D17)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF8B8FA8), modifier = Modifier.size(64.dp))
            Text("Espacio disponible", color = Color(0xFF8B8FA8), fontSize = 14.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = BvTextGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = BvNeonPurp, modifier = Modifier.size(18.dp))
        Text(text, color = BvTextLight, fontSize = 14.sp)
    }
}
