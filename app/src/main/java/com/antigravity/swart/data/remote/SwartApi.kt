package com.antigravity.swart.data.remote

import com.antigravity.swart.data.remote.dto.ExhibitionFeedDto
import com.antigravity.swart.data.remote.dto.MapPinDto
import com.antigravity.swart.data.remote.dto.DiscoverArtworkDto
import com.antigravity.swart.data.remote.dto.SwipeRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SwartApi {
    @GET("api/feed")
    suspend fun getFeed(): List<ExhibitionFeedDto>

    @GET("api/map/balizas")
    suspend fun getMapPins(): List<MapPinDto>
    
    @GET("api/users/{id}/discover")
    suspend fun getDiscoverFeed(@Path("id") userId: Long): List<DiscoverArtworkDto>
    
    @POST("api/swipes")
    suspend fun recordSwipe(@Body request: SwipeRequestDto): Response<Unit>
}
