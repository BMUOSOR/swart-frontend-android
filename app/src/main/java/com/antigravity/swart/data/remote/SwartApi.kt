package com.antigravity.swart.data.remote

import com.antigravity.swart.data.remote.dto.ArtistProfileDto
import com.antigravity.swart.data.remote.dto.ExhibitionDetailDto
import com.antigravity.swart.data.remote.dto.ExhibitionFeedDto
import com.antigravity.swart.data.remote.dto.MapPinDto
import com.antigravity.swart.data.remote.dto.DiscoverArtworkDto
import com.antigravity.swart.data.remote.dto.SwipeRequestDto
import retrofit2.Response
import retrofit2.http.Body
import com.antigravity.swart.data.remote.dto.UpdateExhibitionRequest
import com.antigravity.swart.data.remote.dto.ArtworkDetailDto
import com.antigravity.swart.data.remote.dto.UpdateArtworkRequest
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SwartApi {
    @GET("api/feed")
    suspend fun getFeed(): List<ExhibitionFeedDto>

    @GET("api/map/balizas")
    suspend fun getMapPins(): List<MapPinDto>

    @GET("api/users/{id}/discover")
    suspend fun getDiscoverFeed(@Path("id") userId: Long): List<DiscoverArtworkDto>

    @POST("api/swipes")
    suspend fun recordSwipe(@Body request: SwipeRequestDto): Response<Map<String, String>>

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

    @GET("api/exhibitions/{id}")
    suspend fun getExhibitionDetail(@Path("id") id: Long): ExhibitionDetailDto

    @PUT("api/exhibitions/{id}")
    suspend fun updateExhibition(
        @Path("id") id: Long,
        @Body request: UpdateExhibitionRequest
    ): Map<String, Boolean>

    @DELETE("api/exhibitions/{id}")
    suspend fun deleteExhibition(@Path("id") id: Long): Map<String, Boolean>

    @GET("api/artworks/{id}")
    suspend fun getArtworkDetail(@Path("id") id: Long): ArtworkDetailDto

    @PUT("api/artworks/{id}")
    suspend fun updateArtwork(
        @Path("id") id: Long,
        @Body request: UpdateArtworkRequest
    ): Map<String, Boolean>

    @DELETE("api/artworks/{id}")
    suspend fun deleteArtwork(@Path("id") id: Long): Map<String, Boolean>
}
