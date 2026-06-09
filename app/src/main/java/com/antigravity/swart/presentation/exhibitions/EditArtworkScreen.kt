package com.antigravity.swart.presentation.exhibitions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType

// ── Paleta de Colores Coherente ──────────────────────────────────────────
private val NavyBg         = Color(0xFF0B0D17)
private val CardBg         = Color(0xFF161925)
private val InputBg        = Color(0xFF1E2235)
private val NeonPink       = Color(0xFFFF2D87)
private val NeonPurple     = Color(0xFFEC4899)
private val TextGray       = Color(0xFF8B8FA8)
private val TextLight      = Color(0xFFE8E8F0)
private val neonGradient   = Brush.horizontalGradient(listOf(NeonPurple, NeonPink))

// ── Definición de sub-tags ────────────────────────────────────────────────
private val SUBTAGS_PINTURA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","naturaleza muerta","escena histórica","escena religiosa","escena mitológica","escena de género","abstracción","marina","interior","mural"),
    "Estilo artístico" to listOf("realismo","naturalismo","impresionismo","postimpresionismo","expresionismo","simbolismo","cubismo","surrealismo","fauvismo","abstracto","arte pop","minimalismo","contemporáneo","barroco","renacimiento","neoclasicismo","romanticismo"),
    "Técnica" to listOf("óleo","acrílico","acuarela","gouache","temple","fresco","tinta","pastel","técnica mixta","esmalte","aerosol"),
    "Temática" to listOf("figura humana","retrato psicológico","paisaje natural","paisaje urbano","flora","fauna","mar","arquitectura","vida cotidiana","trabajo","religión","mito","política","memoria","identidad","cuerpo","naturaleza","conflicto","muerte","intimidad")
)

private val SUBTAGS_ESCULTURA = mapOf(
    "Tipo de obra" to listOf("busto","estatua","relieve","bajorrelieve","alto relieve","escultura exenta","escultura monumental","instalación","ensamblaje","escultura pública","objeto escultórico"),
    "Estilo artístico" to listOf("clásico","realismo","barroco","neoclásico","modernismo","expresionismo","cubismo","abstracto","minimalismo","conceptual","contemporáneo","arte povera","orgánico","geométrico"),
    "Técnica" to listOf("talla","modelado","fundición","ensamblaje","soldadura","vaciado","impresión 3D","talla directa","técnica mixta"),
    "Temática" to listOf("figura humana","cuerpo","retrato","monumento","memoria","mito","religión","naturaleza","animal","forma abstracta","espacio","movimiento","identidad","política")
)

private val SUBTAGS_FOTOGRAFIA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","documental","fotoperiodismo","naturaleza muerta","arquitectura","callejera","conceptual","moda","experimental","abstracta"),
    "Estilo artístico" to listOf("documental","realista","pictorialista","modernista","minimalista","conceptual","contemporáneo","experimental","surreal","abstracto"),
    "Técnica" to listOf("analógica","digital","blanco y negro","color","larga exposición","doble exposición","cianotipia","colodión","fotomontaje","intervención digital","macro","estudio"),
    "Temática" to listOf("identidad","cuerpo","memoria","familia","ciudad","paisaje","arquitectura","vida cotidiana","trabajo","política","conflicto","migración","naturaleza","intimidad","tiempo","archivo","comunidad")
)

private fun subtagsForCategoria(cat: String): Map<String, List<String>> = when (cat) {
    "Pintura"    -> SUBTAGS_PINTURA
    "Escultura"  -> SUBTAGS_ESCULTURA
    "Fotografía" -> SUBTAGS_FOTOGRAFIA
    else         -> emptyMap()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditArtworkScreen(
    onBack: () -> Unit,
    onBackWithResult: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    viewModel: EditArtworkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val titulo by viewModel.titulo.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val precioText by viewModel.precioText.collectAsState()
    val disponibleCompra by viewModel.disponibleCompra.collectAsState()
    val categoriasExposicion by viewModel.categoriasExposicion.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val tagsSeleccionados by viewModel.tagsSeleccionados.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditArtworkEvent.SavedSuccessfully -> onBackWithResult("Obra guardada con éxito")
                is EditArtworkEvent.DeletedSuccessfully -> onBackWithResult("Obra eliminada con éxito")
                is EditArtworkEvent.SaveError -> {}
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
                        "home" -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "mapa" -> onNavigateToMap()
                        "perfil" -> onNavigateToProfile()
                        "obras" -> onNavigateToObras()
                    }
                }
            )
        }
    ) { padding ->
        val isSaving = uiState is EditArtworkUiState.Saving

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ─── 1. CABECERA ──────────────────────────────────────────────
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
                    text = "Editar Obra",
                    color = TextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(42.dp))
            }

            // ─── 2. FOTO DE LA OBRA ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen de la obra",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Glassmorphism button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .clickable { /* cambiar foto placeholder */ }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                        Text("Cambiar foto", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ─── 3. FORMULARIO - CAMPOS PRINCIPALES ───────────────────────
            FormField(label = "TÍTULO") {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = viewModel::onTituloChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
            }

            FormField(label = "DESCRIPCIÓN") {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedTextFieldColors(),
                    minLines = 4,
                    maxLines = 6
                )
            }

            // ─── 4. FORMULARIO - PRECIO Y CATEGORÍA ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Precio (Columna Izquierda)
                FormField(label = "PRECIO", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = precioText,
                        onValueChange = viewModel::onPrecioChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("€", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    )
                }

                // Categoría (Columna Derecha)
                FormField(label = "CATEGORIA", modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (categoriaSeleccionada.isBlank()) "Seleccionar" else categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
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
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardBg)
                        ) {
                            if (categoriasExposicion.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Pintura", color = TextLight, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.onCategoriaSelected("Pintura")
                                        expanded = false
                                    }
                                )
                            } else {
                                categoriasExposicion.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = TextLight, fontSize = 13.sp) },
                                        onClick = {
                                            viewModel.onCategoriaSelected(cat)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 5. CATEGORÍA DE LA EXPOSICIÓN ────────────────────────────
            if (categoriasExposicion.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CATEGORÍA DE LA EXPOSICIÓN",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = categoriasExposicion.joinToString(", ").uppercase(),
                        color = NeonPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ─── 6. ETIQUETAS (TAGS) ───────────────────────────────────────
            if (categoriaSeleccionada.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("TAGS ASOCIADOS", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    val subtags = subtagsForCategoria(categoriaSeleccionada)
                    subtags.forEach { (seccion, tags) ->
                        CompactTagSection(
                            seccionName = seccion,
                            allTags = tags,
                            selectedTags = tagsSeleccionados,
                            onTagToggle = viewModel::onTagToggle
                        )
                    }
                }
            }

            // ─── 6. CONFIGURACIÓN DE VENTA (TOGGLE CARD) ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Disponible para compra", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Añade la obra al marketplace", color = TextGray, fontSize = 12.sp)
                }
                GradientSwitch(
                    checked = disponibleCompra,
                    onCheckedChange = viewModel::onDisponibleCompraChange
                )
            }

            // ─── 7. BOTONES DE ACCIÓN ─────────────────────────────────────
            // Guardar cambios
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Borrar obra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteDialog = true }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Borrar obra", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ─── Diálogo de borrado ────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardBg,
            title = { Text("¿Borrar obra?", color = TextLight, fontWeight = FontWeight.Bold) },
            text = { Text("Esta obra se eliminará permanentemente de la base de datos.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteArtwork()
                }) {
                    Text("Borrar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CompactTagSection(
    seccionName: String,
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(seccionName, color = TextGray.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 0.8.sp)
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = InputBg,
                unfocusedContainerColor = InputBg,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                cursorColor = NeonPurple
            ),
            placeholder = { Text("Buscar...", color = TextGray.copy(alpha = 0.5f), fontSize = 12.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
        )

        val sectionSelected = selectedTags.filter { it in allTags }
        val sectionUnselected = allTags.filter { it !in selectedTags && it.contains(searchQuery, ignoreCase = true) }
        val displayedTags = (sectionSelected + sectionUnselected).take(8)

        if (displayedTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                displayedTags.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTagToggle(tag) },
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
        } else {
            Text(
                text = "No se encontraron tags",
                color = TextGray.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─── Componentes Auxiliares ───────────────────────────────────────────────

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

@Composable
private fun GradientSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        label = "thumbOffset"
    )
    Box(
        modifier = Modifier
            .size(52.dp, 30.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (checked) neonGradient else Brush.horizontalGradient(listOf(InputBg, InputBg))
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
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
