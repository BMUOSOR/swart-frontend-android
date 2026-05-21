package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.model.ArtistProfile

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
    suspend fun getMapPins(): Result<List<MapPin>>
    suspend fun getArtistProfile(id: Long): Result<ArtistProfile>
    suspend fun followArtist(artistId: Long, userId: Long): Result<Boolean>
    suspend fun getFollowStatus(artistId: Long, userId: Long): Result<Boolean>
}
