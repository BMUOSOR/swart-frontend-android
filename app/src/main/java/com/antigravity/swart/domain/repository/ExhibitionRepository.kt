package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.MapPin

import com.antigravity.swart.domain.model.DiscoverArtwork

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
    suspend fun getMapPins(): Result<List<MapPin>>
    suspend fun getDiscoverFeed(userId: Long): Result<List<DiscoverArtwork>>
    suspend fun recordSwipe(userId: Long, artworkId: Long, liked: Boolean, matchScore: Double): Result<Unit>
}
