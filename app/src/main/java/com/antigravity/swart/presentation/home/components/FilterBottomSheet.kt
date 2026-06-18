package com.antigravity.swart.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.swart.presentation.theme.InteresadoGradientStart
import java.util.Calendar

// ── Design tokens ─────────────────────────────────────────────────────────────
private val BgDeep          = Color(0xFF0F0F18)
private val BgSurface       = Color(0xFF1A1A27)
private val BgControl       = Color(0xFF22223A)
private val BorderSubtle    = Color(0xFF2E2E4A)
private val TextPrimary     = Color(0xFFF0EEFF)
private val TextSecondary   = Color(0xFF8884AA)
private val TextPlaceholder = Color(0xFF4A4870)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialStartDate: String = "",
    initialEndDate: String = "",
    initialArtistName: String = "",
    initialSelectedTag: String = "Todos",
    // Lista de sugerencias reactiva — el caller la obtiene del ViewModel
    artistSuggestions: List<String> = emptyList(),
    accentColor: Color = InteresadoGradientStart,
    onDismissRequest: () -> Unit,
    // Se llama en cada keystroke para que el ViewModel actualice las sugerencias
    onArtistQueryChanged: (String) -> Unit = {},
    onApplyFilters: (startDate: String, endDate: String, artistName: String, selectedTag: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var artistName  by remember { mutableStateOf(initialArtistName) }
    var selectedTag by remember { mutableStateOf(initialSelectedTag) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    var startDateIso     by remember { mutableStateOf(initialStartDate) }
    var endDateIso       by remember { mutableStateOf(initialEndDate) }
    var startDateDisplay by remember { mutableStateOf(if (initialStartDate.isNotEmpty()) isoToDisplay(initialStartDate) else "") }
    var endDateDisplay   by remember { mutableStateOf(if (initialEndDate.isNotEmpty()) isoToDisplay(initialEndDate) else "") }

    val startPickerState = rememberDatePickerState()
    val endPickerState   = rememberDatePickerState()

    val accentGradient = Brush.horizontalGradient(
        listOf(accentColor, Color(
            red   = (accentColor.red   + 0.15f).coerceAtMost(1f),
            green = (accentColor.green + 0.05f).coerceAtMost(1f),
            blue  = (accentColor.blue  + 0.25f).coerceAtMost(1f),
            alpha = 1f
        ))
    )

    val datePickerColors = DatePickerDefaults.colors(
        selectedDayContainerColor  = accentColor,
        todayDateBorderColor       = accentColor,
        selectedDayContentColor    = Color.White,
        todayContentColor          = accentColor,
        selectedYearContainerColor = accentColor,
        containerColor             = BgSurface,
        titleContentColor          = TextPrimary,
        headlineContentColor       = TextPrimary,
        weekdayContentColor        = TextSecondary,
        subheadContentColor        = TextSecondary,
        yearContentColor           = TextPrimary,
        currentYearContentColor    = accentColor,
        dayContentColor            = TextPrimary,
        disabledDayContentColor    = TextPlaceholder,
        navigationContentColor     = TextPrimary
    )

    // ── DatePicker dialogs ────────────────────────────────────────────────────
    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateIso     = millisToIso(startPickerState.selectedDateMillis)
                    startDateDisplay = millisToDisplay(startPickerState.selectedDateMillis)
                    showStartPicker  = false
                }) { Text("Confirmar", color = accentColor, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = BgSurface)
        ) { DatePicker(state = startPickerState, colors = datePickerColors) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateIso     = millisToIso(endPickerState.selectedDateMillis)
                    endDateDisplay = millisToDisplay(endPickerState.selectedDateMillis)
                    showEndPicker  = false
                }) { Text("Confirmar", color = accentColor, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = BgSurface)
        ) { DatePicker(state = endPickerState, colors = datePickerColors) }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = BgDeep,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FILTROS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Refinar búsqueda",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgControl)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Section: Fecha ────────────────────────────────────────────────
            SectionLabel(
                icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = accentColor, modifier = Modifier.size(14.dp)) },
                text = "Rango de fechas"
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModernDateField(
                    label = "Desde",
                    value = startDateDisplay,
                    accentColor = accentColor,
                    onClear = { startDateIso = ""; startDateDisplay = "" },
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                ModernDateField(
                    label = "Hasta",
                    value = endDateDisplay,
                    accentColor = accentColor,
                    onClear = { endDateIso = ""; endDateDisplay = "" },
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            SectionDivider()

            // ── Section: Artista con autocomplete ─────────────────────────────
            SectionLabel(
                icon = { Icon(Icons.Outlined.Person, null, tint = accentColor, modifier = Modifier.size(14.dp)) },
                text = "Artista"
            )
            Spacer(Modifier.height(10.dp))

            // Campo de texto
            OutlinedTextField(
                value = artistName,
                onValueChange = { value ->
                    artistName = value
                    onArtistQueryChanged(value)
                },
                placeholder = { Text("Buscar artista...", color = TextPlaceholder, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (artistName.isNotEmpty()) {
                            drawLine(
                                color = accentColor,
                                start = Offset(0f, 16f),
                                end = Offset(0f, size.height - 16f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (artistName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Limpiar",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    artistName = ""
                                    onArtistQueryChanged("")
                                }
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = BgControl,
                    focusedContainerColor   = BgControl,
                    unfocusedBorderColor    = BorderSubtle,
                    focusedBorderColor      = accentColor,
                    focusedTextColor        = TextPrimary,
                    unfocusedTextColor      = TextPrimary,
                    cursorColor             = accentColor
                )
            )

            // Dropdown de sugerencias con animación
            AnimatedVisibility(
                visible = artistSuggestions.isNotEmpty(),
                enter = fadeIn(tween(150)) + expandVertically(tween(200)),
                exit  = fadeOut(tween(100)) + shrinkVertically(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    artistSuggestions.forEachIndexed { index, suggestion ->
                        ArtistSuggestionRow(
                            name = suggestion,
                            query = artistName,
                            accentColor = accentColor,
                            showDivider = index < artistSuggestions.lastIndex,
                            onClick = {
                                artistName = suggestion
                                onArtistQueryChanged(suggestion)
                            }
                        )
                    }
                }
            }

            SectionDivider()

            // ── Section: Disciplina ───────────────────────────────────────────
            SectionLabel(text = "Disciplina")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Pintura", "Escultura", "Foto").forEach { tag ->
                    DisciplineChip(
                        label = tag,
                        selected = selectedTag == tag || (tag == "Foto" && selectedTag == "Fotografía"),
                        accentColor = accentColor,
                        onClick = { selectedTag = if (tag == "Foto") "Fotografía" else tag },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Botón aplicar ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = accentGradient)
                    .clickable {
                        onApplyFilters(startDateIso, endDateIso, artistName, selectedTag)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aplicar filtros",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ── ArtistSuggestionRow ───────────────────────────────────────────────────────

@Composable
private fun ArtistSuggestionRow(
    name: String,
    query: String,
    accentColor: Color,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar placeholder con inicial
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nombre con la parte buscada resaltada
            Text(
                text = buildAnnotatedString {
                    val lowerName  = name.lowercase()
                    val lowerQuery = query.lowercase()
                    var cursor = 0
                    var matchIndex = lowerName.indexOf(lowerQuery)
                    while (matchIndex != -1 && query.isNotEmpty()) {
                        // Texto anterior al match
                        append(name.substring(cursor, matchIndex))
                        // Texto resaltado
                        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold)) {
                            append(name.substring(matchIndex, matchIndex + query.length))
                        }
                        cursor = matchIndex + query.length
                        matchIndex = lowerName.indexOf(lowerQuery, cursor)
                    }
                    append(name.substring(cursor))
                },
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = TextPlaceholder,
                modifier = Modifier.size(16.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = BorderSubtle,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(
    text: String,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.invoke()
        Text(
            text = text.uppercase(),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun ModernDateField(
    label: String,
    value: String,
    accentColor: Color,
    onClear: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasValue = value.isNotEmpty()
    val borderColor by animateColorAsState(
        targetValue = if (hasValue) accentColor.copy(alpha = 0.6f) else BorderSubtle,
        animationSpec = tween(200), label = "borderColor"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgControl)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                color = if (hasValue) accentColor else TextPlaceholder,
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (hasValue) value else "dd/mm/aaaa",
                    color = if (hasValue) TextPrimary else TextPlaceholder,
                    fontSize = 13.sp,
                    fontWeight = if (hasValue) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (hasValue) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpiar",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClear() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisciplineChip(
    label: String, selected: Boolean, accentColor: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val bgColor     by animateColorAsState(if (selected) accentColor else BgControl,     tween(200), label = "chipBg")
    val textColor   by animateColorAsState(if (selected) Color.White else TextSecondary, tween(200), label = "chipText")
    val borderColor by animateColorAsState(if (selected) accentColor else BorderSubtle,  tween(200), label = "chipBorder")

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label, color = textColor, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center, letterSpacing = 0.3.sp
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun millisToIso(millis: Long?): String {
    if (millis == null) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH)+1).toString().padStart(2,'0')}-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
}

private fun millisToDisplay(millis: Long?): String {
    if (millis == null) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}/${(cal.get(Calendar.MONTH)+1).toString().padStart(2,'0')}/${cal.get(Calendar.YEAR)}"
}

private fun isoToDisplay(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
}