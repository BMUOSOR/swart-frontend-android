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

@Composable
fun ExhibitionCard(
    exhibition: Exhibition,
    modifier: Modifier = Modifier
) {
    var currentImageIndex by remember { mutableStateOf(0) }

    // Lógica del Slideshow
    LaunchedEffect(exhibition.artworkImagesUrls) {
        if (exhibition.artworkImagesUrls.isNotEmpty()) {
            while (true) {
                delay(3000L) // Cambiar cada 3 segundos
                currentImageIndex = (currentImageIndex + 1) % exhibition.artworkImagesUrls.size
            }
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo con Crossfade para transición suave
            if (exhibition.artworkImagesUrls.isNotEmpty()) {
                Crossfade(
                    targetState = currentImageIndex,
                    animationSpec = tween(1000),
                    label = "imageCrossfade"
                ) { index ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(exhibition.artworkImagesUrls[index])
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

            // Contenido de la tarjeta (Textos y Avatar)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Fila con Avatar y Badge "NUEVO"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = exhibition.artistAvatarUrl,
                            contentDescription = "Artist Avatar",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exhibition.artistName,
                            color = Color.White,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                        )
                    }

                    if (exhibition.isNew) {
                        Box(
                            modifier = Modifier
                                .background(BadgeGreen, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NUEVO",
                                color = Color.White,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                            )
                        }
                    } else if (exhibition.artworksCount > 0) {
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
