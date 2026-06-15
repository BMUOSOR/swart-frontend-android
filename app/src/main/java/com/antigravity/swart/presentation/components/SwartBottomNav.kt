package com.antigravity.swart.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.antigravity.swart.presentation.components.factory.NavItemFactory
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import com.antigravity.swart.presentation.theme.CardBackground
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

enum class UserType {
    ARTIST, GENERAL
}

/**
 * Barra de navegación inferior de SWART.
 *
 * ## Patrón Factory Method aplicado
 * La construcción de los ítems se delega completamente a [NavItemFactory.create].
 * Este composable no conoce qué ítems corresponden a cada rol: solo itera
 * la lista que le devuelve la factoría y los renderiza uniformemente.
 *
 * Esto cumple el **principio Open/Closed**: si se añade un nuevo rol
 * (p. ej. INSTITUTION), únicamente hay que extender [NavItemFactory]
 * sin modificar este fichero.
 *
 * @param userType     Rol del usuario autenticado. Determina los ítems via factoría.
 * @param currentRoute Ruta activa para marcar el ítem seleccionado.
 * @param onNavigate   Callback invocado al pulsar un ítem, recibe la ruta destino.
 * @param onFabClick   Callback del FAB de creación (solo visible para artistas).
 */
@Composable
fun SwartBottomNav(
    userType: UserType,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    badges: Map<String, Long> = emptyMap(),
    onFabClick: () -> Unit = {}
) {
    // La factoría decide qué ítems construir según el rol.
    // remember evita reconstruir la lista en cada recomposición si el rol no cambia.
    val navItems = remember(userType) { NavItemFactory.create(userType) }

    Box(modifier = Modifier.fillMaxWidth()) {
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .shadow(16.dp),
            containerColor = CardBackground,
            contentColor = Color.White
        ) {
            // La UI solo itera el resultado de la factoría, sin lógica de rol
            navItems.forEach { item ->
                BottomNavItem(
                    icon      = item.icon,
                    label     = item.label,
                    selected  = currentRoute == item.route,
                    onClick   = { onNavigate(item.route) },
                    tint      = item.tint,
                    badgeCount = badges[item.route] ?: 0L,
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // El FAB de creación solo aplica al artista
        if (userType == UserType.ARTIST) {
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp),
                containerColor = ArtistaGradientStart,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear exposición")
            }
        }
    }
}

/**
 * Ítem individual de la barra de navegación inferior.
 *
 * Componente puramente visual: renderiza el icono y la etiqueta
 * aplicando el [tint] correspondiente si está seleccionado.
 *
 * @param tint Color activo cuando [selected] es true.
 *             Viene preconfigurado desde [NavItemFactory] con el color del rol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = InteresadoGradientStart,
    // Parámetro legacy mantenido por compatibilidad con llamadas antiguas
    selectedColor: Color = Color.Unspecified,
    badgeCount: Long = 0L
) {
    // Si se pasa selectedColor (llamadas previas a la factoría), lo respeta;
    // si no, usa el tint que viene del BottomNavItem de la factoría.
    val activeTint = when {
        selectedColor != Color.Unspecified -> selectedColor
        tint != Color.Unspecified          -> tint
        else                               -> InteresadoGradientStart
    }
    val iconTint = if (selected) activeTint else Color.Gray

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color(0xFFFF2D87),
                        contentColor = Color.White
                    ) {
                        Text(text = if (badgeCount > 99) "99+" else badgeCount.toString())
                    }
                }
            }
        ) {
            IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                Icon(icon, contentDescription = label, tint = iconTint)
            }
        }
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = iconTint
        )
    }
}
