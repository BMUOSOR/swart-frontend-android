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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.antigravity.swart.presentation.exhibitions.uriToMultipartBodyPart
import com.antigravity.swart.presentation.components.SwartLoadingIndicator

// ─── Paleta ───────────────────────────────────────────────────────────────────
private val BvNavy      = Color(0xFF0B0D17)
private val BvCard      = Color(0xFF161925)
private val BvInput     = Color(0xFF1E2235)
private val BvNeonPink  = Color(0xFFFF2D87)
private val BvNeonPurp  = Color(0xFFEC4899)
private val BvTextGray  = Color(0xFF8B8FA8)
private val BvTextLight = Color(0xFFE8E8F0)
private val BvTextMid   = Color(0xFFB0B4CC)
private val BvGreen     = Color(0xFF10B981)
private val BvPurple    = Color(0xFF8B5CF6)

private val ALL_CATEGORIES = listOf(
    "Pintura", "Escultura", "Fotografía", "Arte Digital",
    "Instalación", "Performance", "Grabado", "Textil", "Mixta"
)

// ─── Tabs ─────────────────────────────────────────────────────────────────────
private enum class DetailTab { INFO, EDITAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalizaVaciaDetailScreen(
    balizaId: Long,
    onBack: () -> Unit,
    onNavigateToProposal: () -> Unit = {},
    onNavigateToArtistProfile: (Long) -> Unit = {},
    viewModel: BalizaVaciaDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(DetailTab.INFO) }

    val context = LocalContext.current

    val balizaItem = uiState.baliza
    LaunchedEffect(balizaItem?.lat, balizaItem?.lon) {
        if (balizaItem != null) {
            viewModel.loadDireccion(context, balizaItem.lat, balizaItem.lon)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val part = uriToMultipartBodyPart(context, uri, "file")
            if (part != null) viewModel.uploadSalaPhoto(part)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            if (it == "Cambios guardados") {
                launch { snackbarHostState.showSnackbar(it) }
                kotlinx.coroutines.delay(800)
                onBack()
                viewModel.clearMessages()
            } else {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessages()
            }
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
                    SwartLoadingIndicator()
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
                    // ─── HERO ─────────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        // Imagen de fondo o placeholder
                        if (uiState.fotosList.isNotEmpty() && uiState.fotosList.first().isNotBlank()) {
                            AsyncImage(
                                model = uiState.fotosList.first(),
                                contentDescription = "Espacio",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            DefaultSpaceBackground()
                        }

                        // Overlay degradado
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Transparent,
                                            BvNavy.copy(alpha = 0.97f)
                                        )
                                    )
                                )
                        )

                        // Botón atrás
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

                        // Título + chips de categorías en la parte inferior del hero
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = baliza.titulo?.ifBlank { "Espacio sin nombre" } ?: "Espacio sin nombre",
                                color = BvTextLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )

                            // Chips de categorías aceptadas
                            val cats = baliza.categorias
                                ?.split(",")
                                ?.map { it.trim() }
                                ?.filter { it.isNotBlank() }

                            if (!cats.isNullOrEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(cats) { cat ->
                                        HeroCategoryChip(cat)
                                    }
                                }
                            }
                        }
                    }

                    // ─── CONTENIDO ────────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Fila propietario + badge disponible
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BvInput)
                                    .border(1.5.dp, BvNeonPurp.copy(alpha = 0.35f), CircleShape)
                            ) {
                                if (!baliza.avatarPropietario.isNullOrBlank()) {
                                    AsyncImage(
                                        model = baliza.avatarPropietario,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle, null,
                                        tint = BvTextGray,
                                        modifier = Modifier.size(22.dp).align(Alignment.Center)
                                    )
                                }
                            }
                            Text(
                                "Espacio de ${baliza.nombrePropietario}",
                                color = BvTextGray,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.weight(1f))
                            // Badge "Disponible"
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BvGreen.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, BvGreen.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    "Disponible",
                                    color = BvGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Stat cards
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(
                                value = baliza.dimensiones?.let { "$it m²" } ?: "—",
                                label = "Superficie",
                                isAccent = true,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = baliza.plantas ?: "—",
                                label = "Plantas",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Tabs
                        val tabs = if (uiState.isPropietario) {
                            listOf(DetailTab.INFO, DetailTab.EDITAR)
                        } else {
                            listOf(DetailTab.INFO)
                        }

                        if (tabs.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BvCard
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tabs.forEach { tab ->
                                        val isActive = selectedTab == tab
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(if (isActive) BvNeonPink else Color.Transparent)
                                                .clickable { selectedTab = tab }
                                                .padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (tab) {
                                                    DetailTab.INFO   -> "Información"
                                                    DetailTab.EDITAR -> "Editar"
                                                },
                                                color = if (isActive) Color.White else BvTextGray,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── TAB: INFORMACIÓN ────────────────────────────────
                        AnimatedVisibility(selectedTab == DetailTab.INFO) {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                // Descripción
                                Text(
                                    baliza.descripcion?.ifBlank { "Sin descripción" } ?: "Sin descripción",
                                    color = BvTextMid,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )

                                Divider(color = Color.White.copy(alpha = 0.06f))

                                // Detalles
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    InfoRow(Icons.Default.Straighten, baliza.dimensiones ?: "No especificadas")
                                    InfoRow(Icons.Default.LocationOn, uiState.direccion ?: "Buscando dirección...")
                                    InfoRow(Icons.Default.CalendarToday, "Disponible desde enero 2025")
                                    InfoRow(Icons.Default.AccessTime, "Acceso 24h con reserva")
                                }
                            }
                        }

                        // ── TAB: EDITAR (solo propietario) ──────────────────
                        AnimatedVisibility(selectedTab == DetailTab.EDITAR && uiState.isPropietario) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                                // Título
                                OutlinedTextField(
                                    value = uiState.titulo,
                                    onValueChange = viewModel::onTituloChange,
                                    label = { Text("Título del espacio", color = BvTextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = outlinedFieldColors()
                                )

                                // Descripción
                                OutlinedTextField(
                                    value = uiState.descripcion,
                                    onValueChange = viewModel::onDescripcionChange,
                                    label = { Text("Describe este espacio", color = BvTextGray) },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = outlinedFieldColors()
                                )

                                // Metros (Superficie)
                                OutlinedTextField(
                                    value = uiState.dimensiones,
                                    onValueChange = viewModel::onDimensionesChange,
                                    label = { Text("Metros cuadrados (m²)", color = BvTextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Straighten, null, tint = BvTextGray) },
                                    colors = outlinedFieldColors()
                                )

                                // Plantas
                                OutlinedTextField(
                                    value = uiState.plantas,
                                    onValueChange = viewModel::onPlantasChange,
                                    label = { Text("Número de plantas", color = BvTextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Layers, null, tint = BvTextGray) },
                                    colors = outlinedFieldColors()
                                )

                                // Categorías
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

                                // Guardar
                                GradientButton(
                                    onClick = viewModel::saveChanges,
                                    enabled = !uiState.isSaving
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(Icons.Default.Save, null, tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.06f))

                        // ── CTA: PROPONER EXPOSICIÓN (solo visitantes) ──────
                        if (!uiState.isPropietario) {
                            GradientButton(onClick = { viewModel.toggleProposalForm(true) }) {
                                Icon(Icons.Default.Send, null, tint = Color.White)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Proponer mi exposición aquí",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }

        if (uiState.isProposalFormOpen) {
            ProposalFormDialog(
                propietarioId = uiState.baliza?.idPropietario ?: -1L,
                currentUserAvatarUrl = viewModel.getCurrentUserAvatarUrl(),
                onSend = { t, d, s, e, c, p, uri -> viewModel.sendProposal(t, d, s, e, c, p, uri) },
                onNavigateToArtistProfile = onNavigateToArtistProfile,
                onDismiss = { viewModel.toggleProposalForm(false) }
            )
        }
    }
}

// ─── Chip de categoría en el hero ─────────────────────────────────────────────
@Composable
private fun HeroCategoryChip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEC4899).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            color = Color(0xFFEC4899),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
        )
    }
}

// ─── Stat card ────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    value: String,
    label: String,
    isAccent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF161925),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAccent) Color(0xFFEC4899) else Color(0xFFE8E8F0)
            )
            Text(text = label, fontSize = 12.sp, color = Color(0xFF8B8FA8))
        }
    }
}

// ─── Botón con degradado ──────────────────────────────────────────────────────
@Composable
private fun GradientButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF2D87), Color(0xFF8B5CF6))
                    ),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

// ─── Fila de info con icono ───────────────────────────────────────────────────
@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
        Text(text, color = Color(0xFFE8E8F0), fontSize = 14.sp)
    }
}

// ─── Colores para OutlinedTextField ──────────────────────────────────────────
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFEC4899),
    unfocusedBorderColor = Color(0xFF1E2235),
    focusedTextColor = Color(0xFFE8E8F0),
    unfocusedTextColor = Color(0xFFE8E8F0),
    cursorColor = Color(0xFFEC4899)
)

// ─── Fondo placeholder ────────────────────────────────────────────────────────
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF8B8FA8), modifier = Modifier.size(64.dp))
            Text("Espacio disponible", color = Color(0xFF8B8FA8), fontSize = 14.sp)
        }
    }
}