package com.antigravity.swart.data.remote

import com.antigravity.swart.data.remote.dto.ArtistProfileDto
import com.antigravity.swart.data.remote.dto.ExhibitionDetailDto
import com.antigravity.swart.data.remote.dto.ExhibitionFeedDto
import com.antigravity.swart.data.remote.dto.MapPinDto
import com.antigravity.swart.data.remote.dto.DiscoverArtworkDto
import com.antigravity.swart.data.remote.dto.SwipeRequestDto
import com.antigravity.swart.data.remote.dto.CreateExhibitionRequest
import com.antigravity.swart.data.remote.dto.CreateArtworkRequest
import com.antigravity.swart.data.remote.dto.MutualArtistDto
import com.antigravity.swart.data.remote.dto.InvitationDto
import com.antigravity.swart.data.remote.dto.ArtistFollowDto
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
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody

interface SwartApi {
    @GET("api/feed")
    suspend fun getFeed(): List<ExhibitionFeedDto>

    @GET("api/map/balizas")
    suspend fun getMapPins(): List<MapPinDto>

    @GET("api/users/{id}/discover")
    suspend fun getDiscoverFeed(@Path("id") userId: Long): List<DiscoverArtworkDto>

    @POST("api/swipes")
    suspend fun recordSwipe(@Body request: SwipeRequestDto): Response<Map<String, String>>

    @GET("api/artists")
    suspend fun getArtists(@Query("userId") userId: Long): List<ArtistFollowDto>

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

    @GET("api/artists/{id}/mutuals")
    suspend fun getMutuals(@Path("id") artistId: Long): List<MutualArtistDto>

    @GET("api/exhibitions/{id}")
    suspend fun getExhibitionDetail(@Path("id") id: Long): ExhibitionDetailDto

    @POST("api/exhibitions")
    suspend fun createExhibition(@Body request: CreateExhibitionRequest): Map<String, Long>

    @PUT("api/exhibitions/{id}")
    suspend fun updateExhibition(
        @Path("id") id: Long,
        @Body request: UpdateExhibitionRequest
    ): Map<String, Boolean>

    @DELETE("api/exhibitions/{id}")
    suspend fun deleteExhibition(@Path("id") id: Long): Map<String, Boolean>

    @GET("api/artworks/{id}")
    suspend fun getArtworkDetail(@Path("id") id: Long): ArtworkDetailDto

    @POST("api/artworks")
    suspend fun createArtwork(@Body request: CreateArtworkRequest): Map<String, Long>

    @PUT("api/artworks/{id}")
    suspend fun updateArtwork(
        @Path("id") id: Long,
        @Body request: UpdateArtworkRequest
    ): Map<String, Boolean>

    @DELETE("api/artworks/{id}")
    suspend fun deleteArtwork(@Path("id") id: Long): Map<String, Boolean>

    @GET("api/map/verify-address")
    suspend fun verifyAddress(
        @Query("address") address: String
    ): com.antigravity.swart.data.remote.dto.GeocodingResultDto

    @GET("api/invitations/{userId}")
    suspend fun getInvitations(@Path("userId") userId: Long): List<InvitationDto>

    @PUT("api/invitations/{id}/respond")
    suspend fun respondInvitation(
        @Path("id") invitationId: Long,
        @Body body: Map<String, Boolean>
    ): Map<String, Boolean>

    @POST("api/chats/start")
    suspend fun startChat(@Body request: com.antigravity.swart.data.remote.dto.StartChatRequest): Map<String, Long>

    @GET("api/chats/user/{userId}")
    suspend fun getConversations(@Path("userId") userId: Long): List<com.antigravity.swart.data.remote.dto.ConversationDto>

    @GET("api/chats/{chatId}/messages")
    suspend fun getChatMessages(@Path("chatId") chatId: Long): List<com.antigravity.swart.data.remote.dto.MessageDto>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendChatMessage(
        @Path("chatId") chatId: Long,
        @Body request: com.antigravity.swart.data.remote.dto.SendMessageRequest
    ): Map<String, String>

    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Map<String, String>
}
