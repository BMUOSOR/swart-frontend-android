package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.ExhibitionDetail
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.model.MapPin


import com.antigravity.swart.domain.model.DiscoverArtwork

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
    suspend fun getMapPins(): Result<List<MapPin>>
    suspend fun getDiscoverFeed(userId: Long): Result<List<DiscoverArtwork>>
    suspend fun recordSwipe(userId: Long, artworkId: Long, liked: Boolean, matchScore: Double): Result<Unit>
    suspend fun getArtistProfile(id: Long): Result<ArtistProfile>
    suspend fun followArtist(artistId: Long, userId: Long): Result<Boolean>
    suspend fun getFollowStatus(artistId: Long, userId: Long): Result<Boolean>
    suspend fun getExhibitionDetail(id: Long): Result<ExhibitionDetail>
    suspend fun updateExhibition(id: Long, titulo: String, descrip: String?, nombreLugar: String?, ubicacion: String?, fechaInicio: String?, fechaFin: String?, tags: List<String>): Result<Boolean>
    suspend fun deleteExhibition(id: Long): Result<Boolean>
}
