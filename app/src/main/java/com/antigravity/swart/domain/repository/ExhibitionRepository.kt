package com.antigravity.swart.domain.repository

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.ExhibitionDetail
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.model.MapPin


import com.antigravity.swart.domain.model.DiscoverArtwork
import com.antigravity.swart.domain.model.ArtworkDetail

import okhttp3.MultipartBody

interface ExhibitionRepository {
    suspend fun getFeed(): Result<List<Exhibition>>
    suspend fun getMapPins(): Result<List<MapPin>>
    suspend fun getDiscoverFeed(userId: Long): Result<List<DiscoverArtwork>>
    suspend fun getLikedArtworks(userId: Long): Result<List<DiscoverArtwork>>
    suspend fun removeLike(userId: Long, obraId: Long): Result<Unit>
    suspend fun recordSwipe(userId: Long, artworkId: Long, liked: Boolean, matchScore: Double): Result<Unit>
    suspend fun getArtistProfile(id: Long): Result<ArtistProfile>
    suspend fun followArtist(artistId: Long, userId: Long): Result<Boolean>
    suspend fun getFollowStatus(artistId: Long, userId: Long): Result<Boolean>
    suspend fun getExhibitionDetail(id: Long): Result<ExhibitionDetail>
    suspend fun updateExhibition(id: Long, titulo: String, descrip: String?, nombreLugar: String?, ubicacion: String?, fechaInicio: String?, fechaFin: String?, imgUrl: String?, tags: List<String>): Result<Boolean>
    suspend fun deleteExhibition(id: Long): Result<Boolean>
    suspend fun getArtworkDetail(id: Long): Result<ArtworkDetail>
    suspend fun updateArtwork(id: Long, titulo: String, descrip: String?, precio: Double?, disponibleCompra: Boolean, tags: List<String>): Result<Boolean>
    suspend fun deleteArtwork(id: Long): Result<Boolean>
    suspend fun verifyAddress(address: String): Result<List<com.antigravity.swart.domain.model.GeocodingResult>>
    suspend fun createExhibition(request: com.antigravity.swart.data.remote.dto.CreateExhibitionRequest): Result<Long>
    suspend fun createArtwork(request: com.antigravity.swart.data.remote.dto.CreateArtworkRequest): Result<Long>
    suspend fun getMutuals(artistId: Long): Result<List<com.antigravity.swart.data.remote.dto.MutualArtistDto>>
    suspend fun getInvitations(userId: Long): Result<List<com.antigravity.swart.data.remote.dto.InvitationDto>>
    suspend fun respondInvitation(invitationId: Long, accept: Boolean): Result<Boolean>
    suspend fun getArtists(userId: Long): Result<List<com.antigravity.swart.data.remote.dto.ArtistFollowDto>>
    suspend fun uploadImage(filePart: MultipartBody.Part): Result<String>
    suspend fun updateAvatar(userId: Long, imageUrl: String): Result<Unit>
    suspend fun startChat(senderId: Long, receiverId: Long, initialMessage: String? = null, urlImagenObra: String? = null): Result<Long>
    suspend fun getConversations(userId: Long): Result<List<com.antigravity.swart.domain.model.Conversation>>
    suspend fun getChatMessages(chatId: Long): Result<List<com.antigravity.swart.domain.model.Message>>
    suspend fun sendChatMessage(chatId: Long, senderId: Long, contenido: String): Result<Boolean>
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<com.antigravity.swart.domain.model.GeocodingResult>
    suspend fun getEmptyBalizas(): Result<List<com.antigravity.swart.domain.model.EmptyBaliza>>
    suspend fun createEmptyBaliza(lat: Double, lon: Double, idPropietario: Long): Result<com.antigravity.swart.domain.model.EmptyBaliza>
    suspend fun deleteEmptyBaliza(id: Long): Result<Boolean>
    suspend fun updateArtistProfile(id: Long, bio: String?, instagram: String?, twitter: String?, correo: String?): Result<Boolean>
    suspend fun incrementExhibitionView(id: Long): Result<Boolean>

    // New map mechanics
    suspend fun getBalizasGubernamentales(): Result<List<com.antigravity.swart.domain.model.GovBaliza>>
    suspend fun createPropuestaBaliza(balizaId: Long, request: com.antigravity.swart.data.remote.dto.PropuestaRequest): Result<Long>
    suspend fun getPropuestasByBaliza(balizaId: Long): Result<List<com.antigravity.swart.domain.model.PropuestaBaliza>>
    suspend fun getPropuestasByArtista(artistaId: Long): Result<List<com.antigravity.swart.domain.model.PropuestaBaliza>>
    suspend fun respondPropuesta(propuestaId: Long, estado: String): Result<Boolean>
    suspend fun getBalizaVaciaDetail(id: Long): Result<com.antigravity.swart.data.remote.dto.BalizaVaciaDetailDto>
    suspend fun updateBalizaVacia(id: Long, request: com.antigravity.swart.data.remote.dto.UpdateBalizaVaciaRequest): Result<Boolean>
    suspend fun getUnreadCount(userId: Long): Result<Long>
}

