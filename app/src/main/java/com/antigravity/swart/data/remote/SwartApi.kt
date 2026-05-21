package com.antigravity.swart.data.remote

import com.antigravity.swart.data.remote.dto.ExhibitionFeedDto
import com.antigravity.swart.data.remote.dto.MapPinDto
import com.antigravity.swart.data.remote.dto.ArtistProfileDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SwartApi {
    @GET("api/feed")
    suspend fun getFeed(): List<ExhibitionFeedDto>

    @GET("api/map/balizas")
    suspend fun getMapPins(): List<MapPinDto>

    @GET("api/artists/{id}")
    suspend fun getArtistProfile(@Path("id") id: Long): ArtistProfileDto

    @POST("api/artists/{id}/follow")
    suspend fun followArtist(
        @Path("id") artistId: Long,
        @Query("userId") userId: Long
    ): Map<String, Boolean>

    @GET("api/artists/{id}/follow")
    suspend fun getFollowStatus(
        @Path("id") artistId: Long,
        @Query("userId") userId: Long
    ): Map<String, Boolean>
}
