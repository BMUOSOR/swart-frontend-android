package com.antigravity.swart.presentation.exhibitions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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

// ── Paleta ────────────────────────────────────────────────────────────────
private val NavyBg         = Color(0xFF0B0D17)
private val CardBg         = Color(0xFF161925)
private val InputBg        = Color(0xFF1E2235)
private val NeonPink       = Color(0xFFFF2D87)
private val NeonPurple     = Color(0xFF7B2FFF)
private val TextGray       = Color(0xFF8B8FA8)
private val TextLight      = Color(0xFFE8E8F0)
private val neonGradient   = Brush.horizontalGradient(listOf(NeonPurple, NeonPink))

// ── Tag definitions ───────────────────────────────────────────────────────
private val CATEGORIAS = listOf("Pintura", "Escultura", "Fotografía")

private val SUBTAGS_PINTURA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","naturaleza muerta","escena histórica","escena religiosa","escena mitológica","abstracción","marina","interior","mural"),
    "Estilo artístico" to listOf("realismo","impresionismo","expresionismo","cubismo","surrealismo","fauvismo","abstracto","arte pop","minimalismo","contemporáneo","barroco","renacimiento"),
    "Técnica" to listOf("óleo","acrílico","acuarela","gouache","temple","fresco","tinta","pastel","técnica mixta","esmalte","aerosol"),
    "Temática" to listOf("figura humana","retrato psicológico","paisaje natural","paisaje urbano","flora","fauna","mar","arquitectura","vida cotidiana","religión","mito","política","identidad")
)
private val SUBTAGS_ESCULTURA = mapOf(
    "Tipo de obra" to listOf("busto","estatua","relieve","bajorrelieve","alto relieve","escultura exenta","escultura monumental","instalación","ensamblaje","objeto escultórico"),
    "Estilo artístico" to listOf("clásico","realismo","barroco","neoclásico","modernismo","expresionismo","cubismo","abstracto","minimalismo","conceptual","contemporáneo"),
    "Técnica" to listOf("talla","modelado","fundición","ensamblaje","soldadura","vaciado","impresión 3D","técnica mixta"),
    "Temática" to listOf("figura humana","cuerpo","retrato","monumento","memoria","mito","religión","naturaleza","animal","forma abstracta","espacio")
)
private val SUBTAGS_FOTOGRAFIA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","documental","fotoperiodismo","naturaleza muerta","arquitectura","callejera","conceptual","moda","experimental","abstracta"),
    "Estilo artístico" to listOf("documental","realista","pictorialista","modernista","minimalista","conceptual","contemporáneo","experimental","surreal","abstracto"),
    "Técnica" to listOf("analógica","digital","blanco y negro","color","larga exposición","doble exposición","cianotipia","colodión","fotomontaje","intervención digital","macro","estudio"),
    "Temática" to listOf("identidad","cuerpo","memoria","familia","ciudad","paisaje","arquitectura","vida cotidiana","trabajo","política","conflicto","migración","naturaleza","tiempo")
)

private fun subtagsForCategoria(cat: String): Map<String, List<String>> = when (cat) {
    "Pintura"    -> SUBTAGS_PINTURA
    "Escultura"  -> SUBTAGS_ESCULTURA
    "Fotografía" -> SUBTAGS_FOTOGRAFIA
    else         -> emptyMap()
}

// ─────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExhibitionScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    viewModel: EditExhibitionViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val titulo         by viewModel.titulo.collectAsState()
    val descripcion    by viewModel.descripcion.collectAsState()
    val nombreLugar    by viewModel.nombreLugar.collectAsState()
    val fechaInicio    by viewModel.fechaInicio.collectAsState()
    val fechaFin       by viewModel.fechaFin.collectAsState()
    val obras          by viewModel.obras.collectAsState()
    val categoria      by viewModel.categoriaSeleccionada.collectAsState()
    val tagsSelec      by viewModel.tagsSeleccionados.collectAsState()
    val bannerUrl      by viewModel.bannerUrl.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // One-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditExhibitionEvent.SavedSuccessfully  -> onBack()
                is EditExhibitionEvent.DeletedSuccessfully -> onNavigateToObras()
                is EditExhibitionEvent.SaveError           -> { /* Snackbar podría añadirse aquí */ }
            }
        }
    }

    Scaffold(
        containerColor = NavyBg,
        bottomBar = {
            SwartBottomNav(
                userType = UserType.ARTIST,
                currentRoute = "obras",
                onNavigate = { route ->
                    when (route) {
                        "home"     -> onNavigateHome()
                        "descubrir"-> onNavigateToSwap()
                        "mapa"     -> onNavigateToMap()
                        "perfil"   -> onNavigateToProfile()
                        "obras"    -> onNavigateToObras()
                    }
                }
            )
        }
    ) { padding ->

        val isSaving = uiState is EditExhibitionUiState.Saving

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ─── 1. HEADER ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(CardBg, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextLight)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Editar expo",
                    color = TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(42.dp)) // balance
            }

            // ─── 2. BANNER ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg),
                contentAlignment = Alignment.Center
            ) {
                if (bannerUrl != null) {
                    AsyncImage(
                        model = bannerUrl,
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Glassmorphism overlay button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .clickable { /* TODO: pick image */ }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                        Text("Cambiar banner", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ─── 3. FORMULARIO PRINCIPAL ─────────────────────────────────
            // Título
            FormField(label = "TÍTULO EXPOSICIÓN") {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = viewModel::onTituloChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
            }

            // Descripción
            FormField(label = "DESCRIPCIÓN") {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedTextFieldColors(),
                    minLines = 4,
                    maxLines = 6
                )
            }

            // Nombre del lugar
            FormField(label = "NOMBRE DEL LUGAR") {
                OutlinedTextField(
                    value = nombreLugar,
                    onValueChange = viewModel::onNombreLugarChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedTextFieldColors(),
                    singleLine = true,
                    trailingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray)
                    }
                )
            }

            // ─── 4. FECHAS ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField(label = "FECHA INICIO", modifier = Modifier.weight(1f)) {
                    DateDropdown(value = fechaInicio, onValueChange = viewModel::onFechaInicioChange)
                }
                FormField(label = "FECHA FIN", modifier = Modifier.weight(1f)) {
                    DateDropdown(value = fechaFin, onValueChange = viewModel::onFechaFinChange)
                }
            }

            // ─── 5. OBRAS ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OBRAS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Text("Editar lista", color = NeonPurple, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* TODO: navigate to artwork list editor */ })
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    obras.forEach { obra ->
                        AsyncImage(
                            model = obra.imageUrl,
                            contentDescription = obra.title,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBg),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Add artwork button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg)
                            .border(
                                BorderStroke(1.5.dp, NeonPurple.copy(alpha = 0.6f)),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { /* TODO: add artwork */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir obra", tint = NeonPurple, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // ─── 6. TAGS / CATEGORÍAS ────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CATEGORÍA & TAGS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)

                // Categoría primaria
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORIAS.forEach { cat ->
                        val isActive = categoria == cat
                        FilterChip(
                            selected = isActive,
                            onClick = { viewModel.onCategoriaSelected(cat) },
                            label = { Text(cat, fontSize = 13.sp) },
                            shape = RoundedCornerShape(50.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.15f),
                                selectedLabelColor = NeonPurple,
                                containerColor = InputBg,
                                labelColor = TextGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selectedBorderColor = NeonPurple,
                                borderColor = Color.Transparent
                            )
                        )
                    }
                }

                // Sub-tags animados
                AnimatedVisibility(
                    visible = categoria.isNotBlank(),
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    val subtags = subtagsForCategoria(categoria)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        subtags.forEach { (seccion, tags) ->
                            Text(seccion, color = TextGray.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 0.8.sp)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                tags.forEach { tag ->
                                    val isSelected = tagsSelec.contains(tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onTagToggle(tag) },
                                        label = { Text(tag, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(50.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonPurple.copy(alpha = 0.15f),
                                            selectedLabelColor = NeonPurple,
                                            containerColor = InputBg,
                                            labelColor = TextGray
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            selectedBorderColor = NeonPurple,
                                            borderColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 7. BOTONES DE ACCIÓN ─────────────────────────────────────
            // Guardar
            Button(
                onClick = { viewModel.saveChanges() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(neonGradient, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteDialog = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar exposición", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ─── Delete confirmation dialog ───────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardBg,
            title = { Text("¿Eliminar exposición?", color = TextLight, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción no se puede deshacer. Se eliminará la exposición y todas sus obras.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteExhibition()
                }) {
                    Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDropdown(value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(14.dp),
            colors = outlinedTextFieldColors(),
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGray
                )
            },
            singleLine = true
        )
        // Date picker simplificado como dropdown de meses
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBg)
        ) {
            listOf("2026-01-01","2026-02-01","2026-03-01","2026-04-01","2026-05-01",
                "2026-06-01","2026-07-01","2026-08-01","2026-09-01","2026-10-01",
                "2026-11-01","2026-12-01","2027-01-01","2027-06-01").forEach { date ->
                DropdownMenuItem(
                    text = { Text(date, color = TextLight, fontSize = 13.sp) },
                    onClick = {
                        onValueChange(date)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = InputBg,
    unfocusedContainerColor = InputBg,
    focusedTextColor = TextLight,
    unfocusedTextColor = TextLight,
    cursorColor = NeonPurple
)
