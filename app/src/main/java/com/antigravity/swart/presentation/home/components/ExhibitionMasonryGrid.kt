package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.swart.domain.model.Exhibition

@Composable
fun ExhibitionMasonryGrid(
    exhibitions: List<Exhibition>,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        // Quitamos el spacing y padding para crear un mosaico perfecto
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalItemSpacing = 0.dp
    ) {
        itemsIndexed(exhibitions) { index, exhibition ->
            // Generamos alturas aleatorias deterministas (basadas en el índice) para el efecto Masonry
            val height = 200 + (index % 3) * 60
            
            ExhibitionCard(
                exhibition = exhibition,
                isFeatured = false, // Las tarjetas del mosaico nunca son destacadas
                modifier = Modifier.height(height.dp)
            )
        }
    }
}
