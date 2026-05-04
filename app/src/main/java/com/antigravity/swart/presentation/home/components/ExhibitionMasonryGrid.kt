package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
        if (exhibitions.isNotEmpty()) {
            // 1. Exposición Destacada (La primera) ocupa toda la fila y hace scroll con el resto
            val featuredExhibition = exhibitions.first()
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    ExhibitionCard(
                        exhibition = featuredExhibition,
                        isFeatured = true,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(180.dp) // Más pequeña como pidió el usuario
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 2. El Mosaico (El resto de exposiciones)
            val mosaicExhibitions = exhibitions.drop(1)
            itemsIndexed(mosaicExhibitions) { index, exhibition ->
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
}
