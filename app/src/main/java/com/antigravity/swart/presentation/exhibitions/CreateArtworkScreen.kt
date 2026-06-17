package com.antigravity.swart.presentation.exhibitions

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import java.util.Calendar

// ── Paleta ────────────────────────────────────────────────────────────────
private val CANavyBg       = Color(0xFF0B0D17)
private val CACardBg       = Color(0xFF161925)
private val CAInputBg      = Color(0xFF1E2235)
private val CANeonPink     = Color(0xFFFF2D87)
private val CANeonPurple   = Color(0xFFEC4899)
private val CATextGray     = Color(0xFF8B8FA8)
private val CATextLight    = Color(0xFFE8E8F0)
private val caNeonGradient = Brush.horizontalGradient(listOf(CANeonPurple, CANeonPink))

private val CA_SUBTAGS_PINTURA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","naturaleza muerta","escena histórica","escena religiosa","escena mitológica","escena de género","abstracción","marina","interior","mural"),
    "Estilo artístico" to listOf("realismo","naturalismo","impresionismo","postimpresionismo","expresionismo","simbolismo","cubismo","surrealismo","fauvismo","abstracto","arte pop","minimalismo","contemporáneo","barroco","renacimiento","neoclasicismo","romanticismo"),
    "Técnica" to listOf("óleo","acrílico","acuarela","gouache","temple","fresco","tinta","pastel","técnica mixta","esmalte","aerosol"),
    "Temática" to listOf("figura humana","retrato psicológico","paisaje natural","paisaje urbano","flora","fauna","mar","arquitectura","vida cotidiana","trabajo","religión","mito","política","memoria","identidad","cuerpo","naturaleza","conflicto","muerte","intimidad")
)
private val CA_SUBTAGS_ESCULTURA = mapOf(
    "Tipo de obra" to listOf("busto","estatua","relieve","bajorrelieve","alto relieve","escultura exenta","escultura monumental","instalación","ensamblaje","escultura pública","objeto escultórico"),
    "Estilo artístico" to listOf("clásico","realismo","barroco","neoclásico","modernismo","expresionismo","cubismo","abstracto","minimalismo","conceptual","contemporáneo","arte povera","orgánico","geométrico"),
    "Técnica" to listOf("talla","modelado","fundición","ensamblaje","soldadura","vaciado","impresión 3D","talla directa","técnica mixta"),
    "Temática" to listOf("figura humana","cuerpo","retrato","monumento","memoria","mito","religión","naturaleza","animal","forma abstracta","espacio","movimiento","identidad","política")
)
private val CA_SUBTAGS_FOTOGRAFIA = mapOf(
    "Tipo de obra" to listOf("retrato","autorretrato","paisaje","paisaje urbano","documental","fotoperiodismo","naturaleza muerta","arquitectura","callejera","conceptual","moda","experimental","abstracta"),
    "Estilo artístico" to listOf("documental","realista","pictorialista","modernista","minimalista","conceptual","contemporáneo","experimental","surreal","abstracto"),
    "Técnica" to listOf("analógica","digital","blanco y negro","color","larga exposición","doble exposición","cianotipia","colodión","fotomontaje","intervención digital","macro","estudio"),
    "Temática" to listOf("identidad","cuerpo","memoria","familia","ciudad","paisaje","arquitectura","vida cotidiana","trabajo","política","conflicto","migración","naturaleza","intimidad","tiempo","archivo","comunidad")
)

private fun caSubtagsForCategoria(cat: String): Map<String, List<String>> = when (cat) {
    "Pintura"    -> CA_SUBTAGS_PINTURA
    "Escultura"  -> CA_SUBTAGS_ESCULTURA
    "Fotografía" -> CA_SUBTAGS_FOTOGRAFIA
    else         -> emptyMap()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateArtworkScreen(
    onBack: () -> Unit,
    onBackWithResult: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    viewModel: CreateArtworkViewModel = hiltViewModel()
) {
    val titulo             by viewModel.titulo.collectAsState()
    val descripcion        by viewModel.descripcion.collectAsState()
    val precioText         by viewModel.precioText.collectAsState()
    val disponibleCompra   by viewModel.disponibleCompra.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val tagsSeleccionados  by viewModel.tagsSeleccionados.collectAsState()
    val isCreating         by viewModel.isCreating.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val imageUrl           by viewModel.imageUrl.collectAsState()
    val isUploading        by viewModel.isUploading.collectAsState()

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val part = uriToMultipartBodyPart(context, it, "file")
                if (part != null) {
                    viewModel.uploadImage(part)
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempUri?.let { uri ->
                    val part = uriToMultipartBodyPart(context, uri, "file")
                    if (part != null) {
                        viewModel.uploadImage(part)
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateArtworkEvent.CreatedSuccessfully -> onBackWithResult("Obra añadida con éxito")
                is CreateArtworkEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showImageSourceDialog) {
        ImageSourceSelectorDialog(
            onDismiss = { showImageSourceDialog = false },
            onGallerySelect = { galleryLauncher.launch("image/*") },
            onCameraSelect = {
                val uri = createTempImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }

    Scaffold(
        containerColor = CANavyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SwartBottomNav(
                userType = UserType.ARTIST,
                currentRoute = "obras",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ─── HEADER ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp).background(CACardBg, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = CATextLight)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Añadir Obra",
                    color = CATextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(42.dp))
            }

            // ─── IMAGEN DE LA OBRA ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CACardBg),
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
                
                if (isUploading) {
                    CircularProgressIndicator(color = CANeonPurple)
                } else {
                    // Glassmorphism overlay button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                            .clickable { showImageSourceDialog = true }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CANeonPurple, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (imageUrl == null) "Añadir foto de obra" else "Cambiar foto",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── TÍTULO ───────────────────────────────────────────────────
            CAFormField(label = "TÍTULO") {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = viewModel::onTituloChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = caOutlinedTextFieldColors(),
                    singleLine = true
                )
            }

            // ─── DESCRIPCIÓN ──────────────────────────────────────────────
            CAFormField(label = "DESCRIPCIÓN") {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = caOutlinedTextFieldColors(),
                    minLines = 4,
                    maxLines = 6
                )
            }

            // ─── PRECIO Y CATEGORÍA ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CAFormField(label = "PRECIO", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = precioText,
                        onValueChange = viewModel::onPrecioChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = caOutlinedTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("€", color = CANeonPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    )
                }
                CAFormField(label = "CATEGORÍA", modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (categoriaSeleccionada.isBlank()) "Seleccionar" else categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = caOutlinedTextFieldColors(),
                            trailingIcon = {
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = CATextGray
                                )
                            },
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CACardBg)
                        ) {
                            listOf("Pintura", "Escultura", "Fotografía").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = CATextLight, fontSize = 13.sp) },
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

            // ─── TAGS ─────────────────────────────────────────────────────
            if (categoriaSeleccionada.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("TAGS ASOCIADOS", color = CATextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    val subtags = caSubtagsForCategoria(categoriaSeleccionada)
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

            // ─── TOGGLE VENTA ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CACardBg)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Disponible para compra", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Añade la obra al marketplace", color = CATextGray, fontSize = 12.sp)
                }
                CAGradientSwitch(
                    checked = disponibleCompra,
                    onCheckedChange = viewModel::onDisponibleCompraChange
                )
            }

            // ─── BOTÓN AÑADIR OBRA ────────────────────────────────────────
            Button(
                onClick = { viewModel.createArtwork() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(caNeonGradient, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Añadir Obra", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
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
        Text(seccionName, color = CATextGray.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 0.8.sp)
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CANeonPurple,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = CAInputBg,
                unfocusedContainerColor = CAInputBg,
                focusedTextColor = CATextLight,
                unfocusedTextColor = CATextLight,
                cursorColor = CANeonPurple
            ),
            placeholder = { Text("Buscar...", color = CATextGray.copy(alpha = 0.5f), fontSize = 12.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = CATextGray, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = CATextGray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
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
                            selectedContainerColor = CANeonPurple.copy(alpha = 0.15f),
                            selectedLabelColor = CANeonPurple,
                            containerColor = CAInputBg,
                            labelColor = CATextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = CANeonPurple,
                            borderColor = Color.Transparent
                        )
                    )
                }
            }
        } else {
            Text(
                text = "No se encontraron tags",
                color = CATextGray.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ── Helpers privados ─────────────────────────────────────────────────────

@Composable
private fun CAFormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = CATextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun CAGradientSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        label = "thumbOffset"
    )
    Box(
        modifier = Modifier
            .size(52.dp, 30.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (checked) caNeonGradient else Brush.horizontalGradient(listOf(CAInputBg, CAInputBg)))
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
private fun caOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CANeonPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = CAInputBg,
    unfocusedContainerColor = CAInputBg,
    focusedTextColor = CATextLight,
    unfocusedTextColor = CATextLight,
    cursorColor = CANeonPurple
)
