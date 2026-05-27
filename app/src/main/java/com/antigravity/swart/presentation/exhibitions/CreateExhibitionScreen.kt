package com.antigravity.swart.presentation.exhibitions

import android.app.DatePickerDialog
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
private val CENeonPurple   = Color(0xFF7B2FFF)
private val CETextGray     = Color(0xFF8B8FA8)
private val CETextLight    = Color(0xFFE8E8F0)
private val ceNeonGradient = Brush.horizontalGradient(listOf(CENeonPurple, CENeonPink))

private val CE_CATEGORIAS = listOf("Pintura", "Escultura", "Fotografía")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExhibitionScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToObras: () -> Unit,
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

    var showMutualsSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateExhibitionEvent.CreatedSuccessfully -> {
                    onNavigateToEdit(event.newId)
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

            // ─── TÍTULO ───────────────────────────────────────────────────
            CEFormField(label = "TÍTULO EXPOSICIÓN") {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = viewModel::onTituloChange,
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
                    value = descripcion,
                    onValueChange = viewModel::onDescripcionChange,
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
                            Box(modifier = Modifier.size(50.dp)) {
                                AsyncImage(
                                    model = artista.avatarUrl,
                                    contentDescription = artista.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CENeonPurple, CircleShape)
                                )
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
                    value = nombreLugar,
                    onValueChange = viewModel::onNombreLugarChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    singleLine = true,
                    placeholder = { Text("Ej. Museo del Prado", color = CETextGray) },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.verifyLocationAddress(fromAddressField = false) }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = CENeonPurple)
                        }
                    }
                )
            }

            // ─── DIRECCIÓN / LOCALIZACIÓN ─────────────────────────────────
            CEFormField(label = "DIRECCIÓN / LOCALIZACIÓN") {
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = viewModel::onUbicacionChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ceOutlinedTextFieldColors(),
                    singleLine = true,
                    placeholder = { Text("Ej. Calle Mayor 1, Madrid", color = CETextGray) },
                    trailingIcon = {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CENeonPurple, strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { viewModel.verifyLocationAddress(fromAddressField = true) }) {
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
