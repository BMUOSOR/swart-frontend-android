package com.antigravity.swart.presentation.components.factory

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de datos que representa un ítem de la barra de navegación inferior.
 *
 * Es el "Producto" del patrón Factory Method: cada instancia encapsula
 * toda la información necesaria para renderizar un elemento de navegación
 * (ruta, icono, etiqueta y tinte opcional), sin que la UI necesite conocer
 * cómo se construyó ni para qué rol está pensado.
 *
 * @param route  Ruta de navegación asociada (debe coincidir con AppNavigation).
 * @param icon   Icono vectorial que se mostrará en la barra.
 * @param label  Texto descriptivo que aparece bajo el icono.
 * @param tint   Color de tinte cuando el ítem está seleccionado.
 *               Por defecto [Color.Unspecified], lo que delega el color
 *               al componente consumidor según el rol del usuario.
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val tint: Color = Color.Unspecified
)
