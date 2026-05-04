package com.antigravity.swart.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.swart.domain.model.Exhibition
import kotlin.random.Random

@Composable
fun ExhibitionMasonryGrid(
    exhibitions: List<Exhibition>,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        itemsIndexed(exhibitions) { index, exhibition ->
            // Generamos alturas aleatorias deterministas (basadas en el índice) para el efecto Masonry
            val height = 200 + (index % 3) * 60
            
            ExhibitionCard(
                exhibition = exhibition,
                modifier = Modifier.height(height.dp)
            )
        }
    }
}
