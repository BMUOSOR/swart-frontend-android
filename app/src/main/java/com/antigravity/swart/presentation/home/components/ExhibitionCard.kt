package com.antigravity.swart.presentation.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.presentation.theme.BadgeGreen
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.TextGray
import com.antigravity.swart.presentation.theme.TextWhite
import kotlinx.coroutines.delay

// ------------------------------------------------------------------
// HERO CARD (Destacada) – squircle horizontal con overlays circulares
// ------------------------------------------------------------------
@Composable
fun FeaturedExhibitionCard(
    exhibition: Exhibition,
    onClick: () -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allImages = remember(exhibition) {
        val list = mutableListOf<String>()
        exhibition.exhibitionImgUrl?.let { list.add(it) }
        list.addAll(exhibition.artworks.map { it.imageUrl })
        list
    }

    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(allImages) {
        if (allImages.size > 1) {
            while (true) {
                delay(if (currentImageIndex == 0 && exhibition.exhibitionImgUrl != null) 8000L else 3000L)
                currentImageIndex = (currentImageIndex + 1) % allImages.size
            }
        }
    }

    Card(
        shape = RoundedCornerShape(28.dp), // Squircle
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo con crossfade
            if (allImages.isNotEmpty()) {
                Crossfade(targetState = currentImageIndex, animationSpec = tween(1000), label = "heroFade") { idx ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(allImages[idx])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen exposición",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(CardBackground))
            }

            // Gradiente oscuro inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 80f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Badge "NUEVO" esquina superior izquierda
            if (exhibition.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(BadgeGreen, RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("NUEVO", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Badge precio esquina superior derecha (translúcido redondeado)
            exhibition.precio?.let { precio ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (precio == 0.0) "Gratis" else "€ ${String.format("%.0f", precio)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Contenido inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                // Artistas con avatares superpuestos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onArtistClick(exhibition.artists.firstOrNull()?.id ?: exhibition.artistId) }
                ) {
                    exhibition.artists.take(3).forEachIndexed { index, artist ->
                        AsyncImage(
                            model = artist.avatarUrl,
                            contentDescription = "Avatar artista",
                            modifier = Modifier
                                .offset(x = if (index > 0) (-8 * index).dp else 0.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .background(Color.Black),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = exhibition.artists.joinToString(", ") { it.name }.ifEmpty { exhibition.artistName },
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Título + lugar
                Text(
                    text = exhibition.title,
                    color = TextWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!exhibition.nombreLugar.isNullOrEmpty()) {
                    Text(
                        text = exhibition.nombreLugar,
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botón acción circular (chevron) esquina inferior derecha
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver exposición",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botón favorito círculo blanco (estrella) esquina superior — solo decorativo por ahora
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = if (exhibition.isNew) 52.dp else 16.dp)
                    // se muestra solo si no hay badge NUEVO ocupando el espacio
            ) { /* espacio reservado */ }
        }
    }
}

// ------------------------------------------------------------------
// TARJETA GRID (Exposiciones próximas) – rectángulo vertical redondeado
// ------------------------------------------------------------------
@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    isFeatured: Boolean = false,
    onClick: () -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isFeatured) {
        FeaturedExhibitionCard(
            exhibition = exhibition,
            onClick = onClick,
            onArtistClick = onArtistClick,
            modifier = modifier
        )
        return
    }

    val allImages = remember(exhibition) {
        val list = mutableListOf<String>()
        exhibition.exhibitionImgUrl?.let { list.add(it) }
        list.addAll(exhibition.artworks.map { it.imageUrl })
        list
    }

    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(allImages) {
        if (allImages.size > 1) {
            while (true) {
                delay(3000L)
                currentImageIndex = (currentImageIndex + 1) % allImages.size
            }
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo
            if (allImages.isNotEmpty()) {
                Crossfade(targetState = currentImageIndex, animationSpec = tween(800), label = "gridFade") { idx ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(allImages[idx])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen exposición",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(CardBackground))
            }

            // Caja de información inferior semitransparente (tercio inferior)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Botón favorito estrella (esquina superior derecha)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorito",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Badge obras (esquina superior izquierda)
            if (exhibition.artworksCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("+${exhibition.artworksCount}", color = Color.White, fontSize = 10.sp)
                }
            }

            // Info inferior: título + artista
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, end = 36.dp, bottom = 10.dp)
            ) {
                Text(
                    text = exhibition.title,
                    color = TextWhite,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exhibition.artistName,
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón chevron circular (esquina inferior derecha)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.White, CircleShape)
                    .clickable { onClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
