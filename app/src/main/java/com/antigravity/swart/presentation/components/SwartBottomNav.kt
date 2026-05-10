package com.antigravity.swart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.antigravity.swart.presentation.theme.DarkSurface
import com.antigravity.swart.presentation.theme.NeonViolet

enum class UserType {
    ARTIST, GENERAL
}

@Composable
fun SwartBottomNav(
    userType: UserType,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(16.dp),
            containerColor = DarkSurface,
            contentColor = Color.White
        ) {
            if (userType == UserType.ARTIST) {
                // Barra de Artista
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Inicio",
                    selected = currentRoute == "home",
                    onClick = { onNavigate("home") },
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Outlined.List,
                    label = "Obras",
                    selected = currentRoute == "obras",
                    onClick = { onNavigate("obras") },
                    modifier = Modifier.weight(1f)
                )
                
                // Espacio vacío para el FAB central
                Spacer(modifier = Modifier.weight(1f))
                
                BottomNavItem(
                    icon = Icons.Outlined.Email,
                    label = "Mensajes",
                    selected = currentRoute == "mensajes",
                    onClick = { onNavigate("mensajes") },
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Perfil",
                    selected = currentRoute == "perfil",
                    onClick = { onNavigate("perfil") },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Barra de Usuario General
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Inicio",
                    selected = currentRoute == "home",
                    onClick = { onNavigate("home") },
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Filled.Star,
                    label = "Descubrir",
                    selected = currentRoute == "descubrir",
                    onClick = { onNavigate("descubrir") },
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Filled.Place,
                    label = "Mapa",
                    selected = currentRoute == "mapa",
                    onClick = { onNavigate("mapa") },
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Filled.Favorite,
                    label = "Favoritos",
                    selected = currentRoute == "favoritos",
                    onClick = { onNavigate("favoritos") },
                    tint = Color.Red, // Favoritos en rojo según la imagen
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Perfil",
                    selected = currentRoute == "perfil",
                    onClick = { onNavigate("perfil") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FAB Central (Solo para Artistas)
        if (userType == UserType.ARTIST) {
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.TopCenter) // Flota sobre la barra
                    .offset(y = (-24).dp) // Lo subimos un poco
                    .size(56.dp),
                shape = CircleShape,
                containerColor = NeonViolet,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Artwork")
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = if (selected) NeonViolet else Color.Gray
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}
