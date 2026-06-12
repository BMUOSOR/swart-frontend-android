package com.antigravity.swart.presentation.components.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

/**
 * Factory Method que centraliza la creación de los ítems de la barra
 * de navegación inferior según el rol del usuario autenticado.
 *
 * ## Patrón aplicado
 * Sigue el patrón **Factory Method** (GoF): la función [create] actúa
 * como método factoría, delegando la construcción concreta a
 * [createArtistItems] o [createGeneralItems] según el tipo de usuario.
 *
 * ## Ventajas
 * - **Open/Closed Principle**: añadir un nuevo rol (p. ej. `INSTITUTION`)
 *   solo requiere agregar un `when` branch y un nuevo método privado,
 *   sin tocar ninguna pantalla existente.
 * - **Elimina condicionales dispersos**: la lógica `if (userType == ARTIST)`
 *   queda confinada aquí, en lugar de repetirse en cada pantalla.
 * - **Coherencia**: todos los ítems de navegación se construyen desde
 *   un único punto de verdad.
 */
object NavItemFactory {

    /**
     * Crea la lista de ítems de navegación correspondiente al [userType].
     *
     * @param userType Tipo de usuario autenticado.
     * @return Lista de [BottomNavItem] configurada para ese rol.
     */
    fun create(userType: UserType): List<BottomNavItem> = when (userType) {
        UserType.ARTIST  -> createArtistItems()
        UserType.GENERAL -> createGeneralItems()
    }

    /**
     * Configuración de navegación para el rol **Artista**.
     *
     * Incluye: Inicio, Obras (gestión de catálogo), Mapa, Mensajes y Perfil.
     * El artista tiene además un FAB de creación gestionado en [SwartBottomNav].
     */
    private fun createArtistItems(): List<BottomNavItem> = listOf(
        BottomNavItem(
            route = "home",
            icon  = Icons.Filled.Home,
            label = "Inicio",
            tint  = ArtistaGradientStart
        ),
        BottomNavItem(
            route = "obras",
            icon  = Icons.Outlined.List,
            label = "Obras",
            tint  = ArtistaGradientStart
        ),
        BottomNavItem(
            route = "mapa",
            icon  = Icons.Filled.Place,
            label = "Mapa",
            tint  = ArtistaGradientStart
        ),
        BottomNavItem(
            route = "mensajes",
            icon  = Icons.Outlined.Email,
            label = "Mensajes",
            tint  = ArtistaGradientStart
        ),
        BottomNavItem(
            route = "perfil",
            icon  = Icons.Outlined.Person,
            label = "Perfil",
            tint  = ArtistaGradientStart
        )
    )

    /**
     * Configuración de navegación para el rol **Interesado** (usuario general).
     *
     * Incluye: Inicio, Descubrir (swipe de obras), Mapa, Favoritos y Perfil.
     */
    private fun createGeneralItems(): List<BottomNavItem> = listOf(
        BottomNavItem(
            route = "home",
            icon  = Icons.Filled.Home,
            label = "Inicio",
            tint  = InteresadoGradientStart
        ),
        BottomNavItem(
            route = "descubrir",
            icon  = Icons.Filled.LocalFireDepartment,
            label = "Descubrir",
            tint  = InteresadoGradientStart
        ),
        BottomNavItem(
            route = "mapa",
            icon  = Icons.Filled.Place,
            label = "Mapa",
            tint  = InteresadoGradientStart
        ),
        BottomNavItem(
            route = "favoritos",
            icon  = Icons.Filled.Favorite,
            label = "Favoritos",
            tint  = InteresadoGradientStart
        ),
        BottomNavItem(
            route = "perfil",
            icon  = Icons.Outlined.Person,
            label = "Perfil",
            tint  = InteresadoGradientStart
        )
    )
}
