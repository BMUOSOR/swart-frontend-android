package com.antigravity.swart.data.repository

import com.antigravity.swart.data.remote.SwartApi
import com.antigravity.swart.data.remote.dto.toDomain
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.MapPin
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
    override suspend fun getDiscoverFeed(userId: Long): Result<List<com.antigravity.swart.domain.model.DiscoverArtwork>> {
        return try {
            val response = api.getDiscoverFeed(userId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordSwipe(userId: Long, artworkId: Long, liked: Boolean, matchScore: Double): Result<Unit> {
        return try {
            val response = api.recordSwipe(com.antigravity.swart.data.remote.dto.SwipeRequestDto(userId, artworkId, liked, matchScore))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error recording swipe: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
