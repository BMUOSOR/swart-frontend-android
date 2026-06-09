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
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

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
            containerColor = CardBackground,
            contentColor = Color.White
        ) {
            if (userType == UserType.ARTIST) {
                // Barra de Artista
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Inicio",
                    selected = currentRoute == "home",
                    onClick = { onNavigate("home") },
                    selectedColor = ArtistaGradientStart,
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Outlined.List,
                    label = "Obras",
                    selected = currentRoute == "obras",
                    onClick = { onNavigate("obras") },
                    selectedColor = ArtistaGradientStart,
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Filled.Place,
                    label = "Mapa",
                    selected = currentRoute == "mapa",
                    onClick = { onNavigate("mapa") },
                    selectedColor = ArtistaGradientStart,
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    icon = Icons.Outlined.Email,
                    label = "Mensajes",
                    selected = currentRoute == "mensajes",
                    onClick = { onNavigate("mensajes") },
                    selectedColor = ArtistaGradientStart,
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Perfil",
                    selected = currentRoute == "perfil",
                    onClick = { onNavigate("perfil") },
                    selectedColor = ArtistaGradientStart,
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
                    icon = Icons.Filled.LocalFireDepartment,
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

    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = InteresadoGradientStart
) {
    val tint = if (selected) selectedColor else Color.Gray
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
