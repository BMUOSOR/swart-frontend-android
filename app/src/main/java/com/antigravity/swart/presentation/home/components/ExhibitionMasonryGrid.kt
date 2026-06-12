package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.presentation.theme.BorderColor
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientStart
import com.antigravity.swart.presentation.theme.TextGray
import com.antigravity.swart.presentation.theme.TextWhite

// Tags predefinidos con iconos para la sección Categorías
private data class TagCategory(val label: String, val icon: ImageVector)

private val KNOWN_TAG_ICONS = mapOf(
    "Pintura"       to Icons.Default.Palette,
    "Fotografía"    to Icons.Default.CameraAlt,
    "Escultura"     to Icons.Default.Category,
    "Ilustración"   to Icons.Default.GridView,
    "Música"        to Icons.Default.MusicNote,
    "Todos"         to Icons.Default.Star
)

// ------------------------------------------------------------------
// GRID PRINCIPAL: LazyColumn con tres secciones
// ------------------------------------------------------------------
@Composable
fun ExhibitionMasonryGrid(
    exhibitions: List<Exhibition>,
    onExhibitionClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    accentColor: Color = InteresadoGradientStart,
    modifier: Modifier = Modifier
) {
    if (exhibitions.isEmpty()) return

    val featuredExhibition = exhibitions.first()
    val restExhibitions = exhibitions.drop(1)

    // Extraer tags únicos de todas las exposiciones para la sección Categorías
    val allTags = remember(exhibitions) {
        val tags = mutableListOf("Todos")
        exhibitions.flatMap { it.tags }.distinct().take(6).forEach { if (it !in tags) tags.add(it) }
        tags
    }

    var selectedCategory by remember { mutableStateOf("Todos") }

    // Exposiciones filtradas por categoría (excluyendo la destacada)
    val filteredExhibitions = remember(selectedCategory, restExhibitions) {
        if (selectedCategory == "Todos") restExhibitions
        else restExhibitions.filter { ex -> ex.tags.any { it.equals(selectedCategory, ignoreCase = true) } }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── SECCIÓN 1: EXPOSICIÓN DESTACADA ──────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                        "Destacada",
                        color = TextWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                Spacer(modifier = Modifier.height(12.dp))

                FeaturedExhibitionCard(
                    exhibition = featuredExhibition,
                    onClick = { onExhibitionClick(featuredExhibition.id) },
                    onArtistClick = onArtistClick,
                    modifier = Modifier.height(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Indicador de carrusel (5 puntos, el central es cápsula)
                CarouselIndicator(total = minOf(5, exhibitions.size), current = 0)
            }
        }

        // ── SECCIÓN 2: CATEGORÍAS ────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Categorías",
                    color = TextWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(allTags) { tag ->
                    CategoryCircleChip(
                        label = tag,
                        icon = KNOWN_TAG_ICONS[tag] ?: Icons.Default.Palette,
                        isSelected = selectedCategory == tag,
                        accentColor = accentColor,
                        onClick = { selectedCategory = tag }
                    )
                }
            }
        }

        // ── SECCIÓN 3: EXPOSICIONES PRÓXIMAS ─────────────────────────
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Pestaña de fecha cóncava / encabezado de sección
                DateSectionHeader(accentColor = accentColor)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Grid 2 columnas con LazyColumn rows
        val rows = filteredExhibitions.chunked(2)
        items(rows) { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                rowItems.forEachIndexed { index, exhibition ->
                    val height = 200 + (filteredExhibitions.indexOf(exhibition) % 3) * 50
                    ExhibitionCard(
                        exhibition = exhibition,
                        isFeatured = false,
                        onClick = { onExhibitionClick(exhibition.id) },
                        onArtistClick = onArtistClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(height.dp)
                    )
                }
                // Si la fila tiene un solo elemento, añadir espacio
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// INDICADOR DE CARRUSEL: puntos + cápsula en el activo
// ------------------------------------------------------------------
@Composable
private fun CarouselIndicator(total: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(total) { index ->
            val isActive = index == current
            if (isActive) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(20.dp)
                        .background(InteresadoGradientStart, RoundedCornerShape(50.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(TextGray.copy(alpha = 0.5f), CircleShape)
                )
            }
            if (index < total - 1) Spacer(modifier = Modifier.width(5.dp))
        }
    }
}

// ------------------------------------------------------------------
// CHIP CIRCULAR DE CATEGORÍA
// ------------------------------------------------------------------
@Composable
private fun CategoryCircleChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) accentColor.copy(alpha = 0.2f) else CardBackground,
                    CircleShape
                )
                .border(
                    1.5.dp,
                    if (isSelected) accentColor else BorderColor,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else TextGray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isSelected) accentColor else TextGray,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ------------------------------------------------------------------
// ENCABEZADO DE SECCIÓN CON FECHA
// ------------------------------------------------------------------
@Composable
private fun DateSectionHeader(accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono calendario
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            ) {
                Text("📅", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Exposiciones",
                color = TextWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
