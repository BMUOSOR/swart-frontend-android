package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.swart.presentation.theme.InteresadoGradientStart
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialStartDate: String = "",
    initialEndDate: String = "",
    initialArtistName: String = "",
    initialSelectedTag: String = "Todos",
    initialArtworkTag: String = "",
    accentColor: Color = InteresadoGradientStart,
    onDismissRequest: () -> Unit,
    onApplyFilters: (startDate: String, endDate: String, artistName: String, selectedTag: String, artworkTag: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate   by remember { mutableStateOf(initialEndDate) }
    var artistName by remember { mutableStateOf(initialArtistName) }
    var selectedTag by remember { mutableStateOf(initialSelectedTag) }
    var artworkTag  by remember { mutableStateOf(initialArtworkTag) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    // Valor ISO (YYYY-MM-DD) que se pasa al ViewModel para comparación
    var startDateIso by remember { mutableStateOf(initialStartDate) }
    var endDateIso   by remember { mutableStateOf(initialEndDate) }
    // Valor legible para mostrar en pantalla
    var startDateDisplay by remember { mutableStateOf(if (initialStartDate.isNotEmpty()) isoToDisplay(initialStartDate) else "") }
    var endDateDisplay   by remember { mutableStateOf(if (initialEndDate.isNotEmpty()) isoToDisplay(initialEndDate) else "") }

    val startPickerState = rememberDatePickerState()
    val endPickerState   = rememberDatePickerState()

    val tags = listOf("Todos", "Pintura", "Escultura", "Fotografía")

    // ── DatePicker dialogs ────────────────────────────────────────────────────
    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateIso     = millisToIso(startPickerState.selectedDateMillis)
                    startDateDisplay = millisToDisplay(startPickerState.selectedDateMillis)
                    showStartPicker  = false
                }) { Text("Aceptar", color = accentColor) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
        ) {
            DatePicker(
                state = startPickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = accentColor,
                    todayDateBorderColor = accentColor,
                    selectedDayContentColor = Color.White,
                    todayContentColor = accentColor,
                    selectedYearContainerColor = accentColor
                )
            )
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateIso     = millisToIso(endPickerState.selectedDateMillis)
                    endDateDisplay = millisToDisplay(endPickerState.selectedDateMillis)
                    showEndPicker  = false
                }) { Text("Aceptar", color = accentColor) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = endPickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = accentColor,
                    todayDateBorderColor = accentColor,
                    selectedDayContentColor = Color.White,
                    todayContentColor = accentColor,
                    selectedYearContainerColor = accentColor
                )
            )
        }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E28),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Fechas
            Text("Buscar por fecha", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerField(
                    label = "Inicio",
                    value = startDateDisplay,
                    accentColor = accentColor,
                    onClear = { startDateIso = ""; startDateDisplay = "" },
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerField(
                    label = "Fin",
                    value = endDateDisplay,
                    accentColor = accentColor,
                    onClear = { endDateIso = ""; endDateDisplay = "" },
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Artista
            Text("Buscar por artista", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                placeholder = { Text("Nombre del artista...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = accentColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Etiquetas de obras
            Text("Buscar por etiquetas de obras", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = artworkTag,
                onValueChange = { artworkTag = it },
                placeholder = { Text("Ej. óleo, retrato, abstracto...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = accentColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Disciplina
            Text("Disciplina", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White,
                            labelColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón aplicar
            Button(
                onClick = { onApplyFilters(startDateIso, endDateIso, artistName, selectedTag, artworkTag) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aplicar Filtros", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    accentColor: Color,
    onClear: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = if (value.isNotEmpty()) accentColor else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (value.isNotEmpty()) value else label,
                color = if (value.isNotEmpty()) Color.White else Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Limpiar",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onClear() }
                )
            }
        }
    }
}

/** "YYYY-MM-DD" para comparación en el ViewModel */
private fun millisToIso(millis: Long?): String {
    if (millis == null) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val y = cal.get(Calendar.YEAR)
    val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$y-$m-$d"
}

/** "DD/MM/YYYY" para mostrar al usuario */
private fun millisToDisplay(millis: Long?): String {
    if (millis == null) return ""
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val y = cal.get(Calendar.YEAR)
    return "$d/$m/$y"
}

/** Convierte "YYYY-MM-DD" a "DD/MM/YYYY" para restaurar el display desde valores iniciales */
private fun isoToDisplay(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
}
