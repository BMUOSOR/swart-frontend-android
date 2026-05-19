package com.antigravity.swart.presentation.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.presentation.theme.BadgeGreen
import kotlinx.coroutines.delay

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    isFeatured: Boolean = false,
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

    // Lógica del Slideshow (5s para la portada, 3s para las obras)
    LaunchedEffect(allImages) {
        if (allImages.isNotEmpty() && allImages.size > 1) {
            while (true) {
                val delayTime = if (currentImageIndex == 0 && exhibition.exhibitionImgUrl != null) 8000L else 3000L
                delay(delayTime)
                currentImageIndex = (currentImageIndex + 1) % allImages.size
            }
        }
    }

    // La destacada tiene bordes redondeados, las del mosaico encajan perfectas sin bordes
    val cardShape = if (isFeatured) RoundedCornerShape(0.dp) else RoundedCornerShape(0.dp)

    Card(
        shape = cardShape,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo
            if (allImages.isNotEmpty()) {
                Crossfade(
                    targetState = currentImageIndex,
                    animationSpec = tween(1000),
                    label = "imageCrossfade"
                ) { index ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(allImages[index])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artwork image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                )
            }

            // Gradiente oscuro en la parte inferior para legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Badge "NUEVO" solo si es destacada (esquina superior derecha)
            if (isFeatured && exhibition.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(BadgeGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NUEVO",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Contenido inferior (Avatar, textos y badge de conteo)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val artistsList = exhibition.artists
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onArtistClick(artistsList.firstOrNull()?.id ?: exhibition.artistId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            artistsList.take(3).forEachIndexed { index, artist ->
                                val offset = if (index > 0) (-8 * index).dp else 0.dp
                                AsyncImage(
                                    model = artist.avatarUrl,
                                    contentDescription = "Artist Avatar",
                                    modifier = Modifier
                                        .offset(x = offset)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        val overlapOffset = if (artistsList.take(3).size > 1) {
                            (-8 * (artistsList.take(3).size - 1)).dp
                        } else {
                            0.dp
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp).offset(x = overlapOffset))
                        
                        Text(
                            text = if (artistsList.isNotEmpty()) artistsList.joinToString(", ") { it.name } else exhibition.artistName,
                            color = Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(x = overlapOffset)
                        )
                    }

                    // Badge de Obras
                    if (exhibition.artworksCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${exhibition.artworksCount} Obras",
                                color = Color.White,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Text(
                    text = exhibition.title,
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción
                if (!exhibition.description.isNullOrEmpty()) {
                    Text(
                        text = exhibition.description,
                        color = Color.LightGray,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
