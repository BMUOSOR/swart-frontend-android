package com.antigravity.swart.presentation.exhibitions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import java.time.LocalDate

@Composable
fun MyExhibitionsScreen(
    onNavigateHome: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    successMessage: String? = null,
    onClearSuccessMessage: () -> Unit = {},
    viewModel: MyExhibitionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Color definitions
    val DeepNavyBackground = Color(0xFF0B0D17) // Fondo azul marino casi negro
    val LilaAccent = Color(0xFF8B5CF6) // Morado/lila neón
    val CardNavyBackground = Color(0xFF161925) // Fondo oscuro de las tarjetas
    val TextGrayLight = Color(0xFFA0A0AB) // Gris claro para los subtítulos

    Scaffold(
        containerColor = DeepNavyBackground,
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
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Cabecera (Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mis Exposiciones",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gestiona tus muestras actuales",
                        color = TextGrayLight,
                        fontSize = 14.sp
                    )
                }

                // Botón cuadrado con bordes muy redondeados de color morado sólido
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LilaAccent)
                        .clickable { /* Crear exposición */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir Exposición",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // State Handling
            when (val state = uiState) {
                is MyExhibitionsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LilaAccent)
                    }
                }
                is MyExhibitionsUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                is MyExhibitionsUiState.Success -> {
                    val exhibitions = state.profile.activeExhibitions
                    if (exhibitions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no tienes exposiciones registradas.",
                                color = TextGrayLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        exhibitions.forEach { exhibition ->
                            val isActive = remember(exhibition.fechaFin) {
                                val fechaFinStr = exhibition.fechaFin
                                if (fechaFinStr == null) {
                                    true
                                } else {
                                    try {
                                        val endDate = LocalDate.parse(fechaFinStr)
                                        !endDate.isBefore(LocalDate.now())
                                    } catch (e: Exception) {
                                        true
                                    }
                                }
                            }

                            ExhibitionItemCard(
                                exhibition = exhibition,
                                isActive = isActive,
                                lilaAccent = LilaAccent,
                                cardBackground = CardNavyBackground,
                                textGrayLight = TextGrayLight,
                                onDetailClick = onNavigateToDetail,
                                onEditClick = { onNavigateToEdit(exhibition.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = onClearSuccessMessage,
            containerColor = Color(0xFF161925),
            title = { Text("Éxito", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(successMessage, color = Color(0xFF8B8FA8)) },
            confirmButton = {
                TextButton(onClick = onClearSuccessMessage) {
                    Text("Aceptar", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ExhibitionItemCard(
    exhibition: Exhibition,
    isActive: Boolean,
    lilaAccent: Color,
    cardBackground: Color,
    textGrayLight: Color,
    onDetailClick: (Long) -> Unit,
    onEditClick: () -> Unit = {}
) {
    val dateText = "${exhibition.fechaInicio ?: ""} - ${exhibition.fechaFin ?: ""} • ${exhibition.nombreLugar ?: "Galería Sol"}"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = if (isActive) BorderStroke(1.dp, lilaAccent.copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header de la Tarjeta (Fila)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = exhibition.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateText,
                        color = textGrayLight,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge en forma de píldora
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) Color(0xFF10B981) else Color(0xFF6B7280))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isActive) "ACTIVA" else "FINALIZADA",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón circular de editar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242838))
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar exposición",
                            tint = textGrayLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Contenido de la Tarjeta (Fila de imágenes y bloque derecho)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val artworks = exhibition.artworks
                val artwork1 = artworks.getOrNull(0)
                val artwork2 = artworks.getOrNull(1)

                // Imagen Izquierda (obra 1 de la expo)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242838))
                ) {
                    if (artwork1 != null) {
                        AsyncImage(
                            model = artwork1.imageUrl,
                            contentDescription = artwork1.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No hay obra",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Degradado inferior para legibilidad del texto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                    startY = 40f
                                )
                            )
                    )

                    // Texto superpuesto inferior izquierdo + lápiz edición translúcido
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = artwork1?.title ?: "Obra 1",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { /* Editar obra 1 */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar obra",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }

                // Imagen Centro (obra 2 de la expo)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF242838))
                ) {
                    if (artwork2 != null) {
                        AsyncImage(
                            model = artwork2.imageUrl,
                            contentDescription = artwork2.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No hay obra",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Degradado inferior para legibilidad del texto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                    startY = 40f
                                )
                            )
                    )

                    // Texto superpuesto en esquina inferior izquierda
                    Text(
                        text = artwork2?.title ?: "Obra 2",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    )

                    // Icono de lápiz translúcido en esquina superior derecha
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { /* Editar obra 2 */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar obra",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }

                // Bloque Derecho (Texto centrado verticalmente)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    val totalObras = exhibition.artworksCount
                    val remaining = maxOf(0, totalObras - 2)

                    Text(
                        text = "+ $remaining obras",
                        color = textGrayLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ver catálogo completo",
                        color = lilaAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDetailClick(exhibition.id) }
                    )
                }
            }
        }
    }
}
