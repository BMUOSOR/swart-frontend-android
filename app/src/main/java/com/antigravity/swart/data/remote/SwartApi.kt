package com.antigravity.swart.data.remote

import com.antigravity.swart.data.remote.dto.ExhibitionFeedDto
import com.antigravity.swart.data.remote.dto.MapPinDto
import retrofit2.http.GET

interface SwartApi {
    @GET("api/feed")
    suspend fun getFeed(): List<ExhibitionFeedDto>

    @GET("api/map/balizas")
    suspend fun getMapPins(): List<MapPinDto>
}
