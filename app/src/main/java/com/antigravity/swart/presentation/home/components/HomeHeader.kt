package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientStart
import com.antigravity.swart.presentation.theme.TextGray
import com.antigravity.swart.presentation.theme.TextWhite

// ------------------------------------------------------------------
// TOP BAR: avatar perfil (izq) + título izquierdo + icono favoritos (der)
// ------------------------------------------------------------------
@Composable
fun HomeTopBar(
    avatarUrl: String,
    accentColor: Color = InteresadoGradientStart,
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar (izq) → navega al perfil
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, accentColor, CircleShape)
                .padding(3.dp)
                .clickable { onNavigateToPerfil() }
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Perfil de usuario",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )
        }

        // Título alineado a la izquierda justo a la derecha del botón
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = "Descubrir",
                style = MaterialTheme.typography.titleLarge,
                color = TextWhite
            )
            Text(
                text = "Explora nuevas visiones artísticas",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                fontSize = 11.sp
            )
        }

        // Icono favoritos (der) → navega a favoritos
        IconButton(
            onClick = onNavigateToFavoritos,
            modifier = Modifier
                .size(40.dp)
                .background(CardBackground, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favoritos",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ------------------------------------------------------------------
// BARRA DE BÚSQUEDA: Forma cápsula + icono de sliders (filtros)
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit = {},
    accentColor: Color = InteresadoGradientStart
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Buscador tipo cápsula
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar artistas u obras", color = TextGray, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextGray, modifier = Modifier.size(20.dp))
            },
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = CardBackground,
                focusedContainerColor = CardBackground,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = accentColor
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        )

        // Botón filtro: círculo más contenido con icono de sliders centrado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(accentColor, CircleShape)
                .clickable { onFilterClick() }
        ) {
            SlidersIcon()
        }
    }
}

// Icono de sliders: 3 líneas con deslizadores
@Composable
private fun SlidersIcon() {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(20.dp)
    ) {
        SliderLine(thumbOffset = 0.3f)
        SliderLine(thumbOffset = 0.65f)
        SliderLine(thumbOffset = 0.2f)
    }
}

@Composable
private fun SliderLine(thumbOffset: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)))
        Box(
            modifier = Modifier
                .fillMaxWidth(thumbOffset)
                .wrapContentWidth(Alignment.End)
                .align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White, CircleShape)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}