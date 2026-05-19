package com.antigravity.swart.data.repository

import com.antigravity.swart.data.remote.SwartApi
import com.antigravity.swart.data.remote.dto.toDomain
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.repository.ExhibitionRepository

class ExhibitionRepositoryImpl(
    private val api: SwartApi
) : ExhibitionRepository {

    override suspend fun getFeed(): Result<List<Exhibition>> {
        return try {
            val response = api.getFeed()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMapPins(): Result<List<MapPin>> {
        return try {
            val response = api.getMapPins()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArtistProfile(id: Long): Result<ArtistProfile> {
        return try {
            val response = api.getArtistProfile(id)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun followArtist(artistId: Long, userId: Long): Result<Boolean> {
        return try {
            val response = api.followArtist(artistId, userId)
            Result.success(response["following"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
