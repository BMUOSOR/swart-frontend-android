package com.antigravity.swart.presentation.exhibitions

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.data.remote.dto.MutualArtistDto
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import java.util.Calendar

// ── Paleta ────────────────────────────────────────────────────────────────
private val CENavyBg       = Color(0xFF0B0D17)
private val CECardBg       = Color(0xFF161925)
private val CEInputBg      = Color(0xFF1E2235)
private val CENeonPink     = Color(0xFFFF2D87)
private val CENeonPurple   = Color(0xFFEC4899)
private val CETextGray     = Color(0xFF8B8FA8)
private val CETextLight    = Color(0xFFE8E8F0)
private val ceNeonGradient = Brush.horizontalGradient(listOf(CENeonPurple, CENeonPink))

private val CE_CATEGORIAS = listOf("Pintura", "Escultura", "Fotografía")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExhibitionScreen(
    onBack: () -> Unit,
    onBackWithResult: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
    onNavigateToMensajes: () -> Unit = {},
    viewModel: CreateExhibitionViewModel = hiltViewModel()
) {
    val titulo         by viewModel.titulo.collectAsState()
    val descripcion    by viewModel.descripcion.collectAsState()
    val nombreLugar    by viewModel.nombreLugar.collectAsState()
    val ubicacion      by viewModel.ubicacion.collectAsState()
    val fechaInicio    by viewModel.fechaInicio.collectAsState()
    val fechaFin       by viewModel.fechaFin.collectAsState()
    val categoria      by viewModel.categoria.collectAsState()
    val isColaborativa by viewModel.isColaborativa.collectAsState()
    val artistasMutuos by viewModel.artistasMutuos.collectAsState()
    val artistasInv    by viewModel.artistasInvitados.collectAsState()
    val isCreating     by viewModel.isCreating.collectAsState()
    val isVerifying    by viewModel.isVerifying.collectAsState()
    val verifSuccess   by viewModel.verificationSuccess.collectAsState()
    val verifError     by viewModel.verificationError.collectAsState()
    val bannerUrl      by viewModel.bannerUrl.collectAsState()
    val isUploading    by viewModel.isUploading.collectAsState()
    val isFromMap         by viewModel.isFromMap.collectAsState()
    val isLoadingAddress  by viewModel.isLoadingAddress.collectAsState()

    var showMutualsSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // ── TextFieldValue local states (preserva composición IME para ñ, acentos, etc.) ──
    var tituloTfv      by remember { mutableStateOf(TextFieldValue(titulo)) }
    var descripcionTfv by remember { mutableStateOf(TextFieldValue(descripcion)) }
    var nombreLugarTfv by remember { mutableStateOf(TextFieldValue(nombreLugar)) }
    var ubicacionTfv   by remember { mutableStateOf(TextFieldValue(ubicacion)) }
    // Sincronizar cuando el ViewModel actualiza los campos de localización externamente
    LaunchedEffect(nombreLugar) { if (nombreLugar != nombreLugarTfv.text) nombreLugarTfv = TextFieldValue(nombreLugar) }
    LaunchedEffect(ubicacion)   { if (ubicacion   != ubicacionTfv.text)   ubicacionTfv   = TextFieldValue(ubicacion)   }

    // uCrop result handler — receives cropped image and uploads it
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                val part = uriToMultipartBodyPart(context, uri, "file")
                if (part != null) viewModel.uploadBanner(part)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val destFile = File(context.cacheDir, "banner_${UUID.randomUUID()}.jpg")
                val uCropIntent = UCrop.of(it, Uri.fromFile(destFile))
                    .withAspectRatio(16f, 9f)
                    .withMaxResultSize(1920, 1080)
                    .getIntent(context)
                uCropLauncher.launch(uCropIntent)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempUri?.let { uri ->
                    val destFile = File(context.cacheDir, "banner_${UUID.randomUUID()}.jpg")
                    val uCropIntent = UCrop.of(uri, Uri.fromFile(destFile))
                        .withAspectRatio(16f, 9f)
                        .withMaxResultSize(1920, 1080)
                        .getIntent(context)
                    uCropLauncher.launch(uCropIntent)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateExhibitionEvent.CreatedSuccessfully -> {
                    onBackWithResult("Exposición creada correctamente")
                }
                is CreateExhibitionEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = CENavyBg,
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
                        "mensajes"  -> onNavigateToMensajes()
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
            // ─── HEADER ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp).background(CECardBg, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = CETextLight)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Nueva Exposición",
                    color = CETextLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(42.dp))
            }

            // ─── BANNER ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CECardBg),
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
                if (isUploading) {
                    CircularProgressIndicator(color = CENeonPurple)
                } else {
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
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CENeonPurple, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (bannerUrl == null) "Añadir banner" else "Cambiar banner",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── TÍTULO ───────────────────────────────────────────────────
            CEFormField(label = "TÍTULO EXPOSICIÓN") {
                OutlinedTextField(
                    value = tituloTfv,
                    onValueChange = { tituloTfv = it; viewModel.onTituloChange(it.text) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    singleLine = true,
                    placeholder = { Text("Ej. Horizontes del Color", color = CETextGray) }
                )
            }

            // ─── DESCRIPCIÓN ──────────────────────────────────────────────
            CEFormField(label = "DESCRIPCIÓN") {
                OutlinedTextField(
                    value = descripcionTfv,
                    onValueChange = { descripcionTfv = it; viewModel.onDescripcionChange(it.text) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    minLines = 4,
                    maxLines = 6,
                    placeholder = { Text("Describe tu exposición...", color = CETextGray) }
                )
            }

            // ─── SWITCH COLABORATIVA ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CECardBg)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Exposición Colaborativa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Invita a otros artistas a colaborar", color = CETextGray, fontSize = 12.sp)
                }
                CEGradientSwitch(
                    checked = isColaborativa,
                    onCheckedChange = viewModel::onColaborativaToggle
                )
            }

            // ─── SECCIÓN ARTISTAS COLABORADORES ──────────────────────────
            AnimatedVisibility(
                visible = isColaborativa,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "ARTISTAS COLABORADORES",
                        color = CETextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // Artistas ya añadidos
                        artistasInv.forEach { artista ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable { viewModel.toggleInvitado(artista) },
                                contentAlignment = Alignment.TopEnd
                            ) {
                                AsyncImage(
                                    model = artista.avatarUrl,
                                    contentDescription = artista.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.BottomStart)
                                        .clip(CircleShape)
                                        .border(2.dp, CENeonPurple, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                        // Botón añadir
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(CECardBg)
                                .border(1.5.dp, CENeonPurple.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    viewModel.loadMutuals()
                                    showMutualsSheet = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir artista", tint = CENeonPurple)
                        }
                    }
                    if (artistasInv.isEmpty()) {
                        Text(
                            "Pulsa + para añadir artistas con seguimiento mutuo",
                            color = CETextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ─── NOMBRE DEL LUGAR ─────────────────────────────────────────
            CEFormField(label = "NOMBRE DEL LUGAR") {
                OutlinedTextField(
                    value = nombreLugarTfv,
                    onValueChange = { if (!isFromMap) { nombreLugarTfv = it; viewModel.onNombreLugarChange(it.text) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    singleLine = true,
                    readOnly = isFromMap,
                    placeholder = { Text("Ej. Museo del Prado", color = CETextGray) },
                    trailingIcon = {
                        when {
                            isLoadingAddress -> CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CENeonPurple, strokeWidth = 2.dp)
                            isFromMap -> Icon(Icons.Default.Lock, contentDescription = "Fijado desde el mapa", tint = CETextGray, modifier = Modifier.size(20.dp))
                            else -> IconButton(onClick = { viewModel.verifyLocationAddress(fromAddressField = false) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = CENeonPurple)
                            }
                        }
                    }
                )
            }

            // ─── DIRECCIÓN / LOCALIZACIÓN ─────────────────────────────────
            CEFormField(label = "DIRECCIÓN / LOCALIZACIÓN") {
                OutlinedTextField(
                    value = ubicacionTfv,
                    onValueChange = { if (!isFromMap) { ubicacionTfv = it; viewModel.onUbicacionChange(it.text) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    singleLine = true,
                    readOnly = isFromMap,
                    placeholder = {
                        if (isLoadingAddress) Text("Obteniendo dirección...", color = CETextGray)
                        else Text("Ej. Calle Mayor 1, Madrid", color = CETextGray)
                    },
                    trailingIcon = {
                        when {
                            isLoadingAddress -> CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CENeonPurple, strokeWidth = 2.dp)
                            isFromMap -> Icon(Icons.Default.Lock, contentDescription = "Fijado desde el mapa", tint = CETextGray, modifier = Modifier.size(20.dp))
                            isVerifying -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CENeonPurple, strokeWidth = 2.dp)
                            else -> IconButton(onClick = { viewModel.verifyLocationAddress(fromAddressField = true) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar dirección", tint = CENeonPurple)
                            }
                        }
                    }
                )
                if (verifSuccess != null) {
                    Text(verifSuccess!!, color = Color(0xFF10B981), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                }
                if (verifError != null) {
                    Text(verifError!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                }
            }

            // ─── FECHAS ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CEFormField(label = "FECHA INICIO", modifier = Modifier.weight(1f)) {
                    CEDatePickerField(value = fechaInicio, onValueChange = viewModel::onFechaInicioChange)
                }
                CEFormField(label = "FECHA FIN", modifier = Modifier.weight(1f)) {
                    CEDatePickerField(value = fechaFin, onValueChange = viewModel::onFechaFinChange)
                }
            }

            // ─── CATEGORÍA ────────────────────────────────────────────────
            CEFormField(label = "CATEGORÍA") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CE_CATEGORIAS.forEach { cat ->
                        val isSelected = categoria == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategoriaSelected(cat) },
                            label = { Text(cat, fontSize = 13.sp) },
                            shape = RoundedCornerShape(50.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CENeonPurple.copy(alpha = 0.15f),
                                selectedLabelColor = CENeonPurple,
                                containerColor = CEInputBg,
                                labelColor = CETextGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selectedBorderColor = CENeonPurple,
                                borderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // ─── BOTÓN CREAR ──────────────────────────────────────────────
            Button(
                onClick = { viewModel.createExhibition() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(ceNeonGradient, RoundedCornerShape(16.dp)),
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
                            Text("Crear Exposición", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ─── BottomSheet artistas mutuos ──────────────────────────────────────
    if (showMutualsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMutualsSheet = false },
            containerColor = CECardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Artistas con seguimiento mutuo",
                    color = CETextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (artistasMutuos.isEmpty()) {
                    Text(
                        "No tienes artistas con seguimiento mutuo aún.",
                        color = CETextGray,
                        fontSize = 14.sp
                    )
                } else {
                    artistasMutuos.forEach { artista ->
                        val isSelected = artistasInv.any { it.id == artista.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CENeonPurple.copy(alpha = 0.1f) else CEInputBg)
                                .clickable { viewModel.toggleInvitado(artista) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = artista.avatarUrl,
                                contentDescription = artista.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CEInputBg)
                            )
                            Text(
                                artista.nombre,
                                color = CETextLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleInvitado(artista) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CENeonPurple,
                                    uncheckedColor = CETextGray
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showMutualsSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CENeonPurple)
                ) {
                    Text("Confirmar selección", color = Color.White, fontWeight = FontWeight.Bold)
                }
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
}

// ── Helpers privados ─────────────────────────────────────────────────────

@Composable
private fun CEFormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = CETextGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun CEDatePickerField(value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            onValueChange(String.format("%04d-%02d-%02d", y, m + 1, d))
        }, year, month, day)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ceOutlinedTextFieldColors(),
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = CETextGray) },
            singleLine = true
        )
        Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
    }
}

@Composable
private fun CEGradientSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        label = "thumbOffset"
    )
    Box(
        modifier = Modifier
            .size(52.dp, 30.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (checked) ceNeonGradient else Brush.horizontalGradient(listOf(CEInputBg, CEInputBg)))
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
private fun ceOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CENeonPurple,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = CEInputBg,
    unfocusedContainerColor = CEInputBg,
    focusedTextColor = CETextLight,
    unfocusedTextColor = CETextLight,
    cursorColor = CENeonPurple
)
