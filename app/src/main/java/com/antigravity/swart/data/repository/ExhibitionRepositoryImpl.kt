package com.antigravity.swart.data.repository

import com.antigravity.swart.data.remote.SwartApi
import com.antigravity.swart.data.remote.dto.UpdateExhibitionRequest
import com.antigravity.swart.data.remote.dto.toDomain
import com.antigravity.swart.domain.model.Artwork
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.ExhibitionDetail
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.repository.ExhibitionRepository
import com.antigravity.swart.domain.model.ArtworkDetail

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

    override suspend fun getFollowStatus(artistId: Long, userId: Long): Result<Boolean> {
        return try {
            val response = api.getFollowStatus(artistId, userId)
            Result.success(response["following"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExhibitionDetail(id: Long): Result<ExhibitionDetail> {
        return try {
            val dto = api.getExhibitionDetail(id)
            Result.success(
                ExhibitionDetail(
                    idExposicion = dto.idExposicion,
                    titulo = dto.titulo,
                    descrip = dto.descrip,
                    nombreLugar = dto.nombreLugar,
                    ubicacion = dto.ubicacion,
                    fechaInicio = dto.fechaInicio,
                    fechaFin = dto.fechaFin,
                    imgUrl = dto.imgUrl,
                    precio = dto.precio,
                    score = dto.score,
                    activa = dto.activa,
                    obras = dto.obras.map { Artwork(it.idObra, it.titulo, it.imgUrl, it.idArtista) },
                    tags = dto.tags,
                    esColaborativa = dto.esColaborativa,
                    artistas = dto.artistas.map {
                        com.antigravity.swart.domain.model.ArtistBasic(it.id, it.nombre, it.avatarUrl)
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExhibition(
        id: Long, titulo: String, descrip: String?, nombreLugar: String?,
        ubicacion: String?, fechaInicio: String?, fechaFin: String?, imgUrl: String?, tags: List<String>
    ): Result<Boolean> {
        return try {
            val response = api.updateExhibition(
                id, UpdateExhibitionRequest(titulo, descrip, nombreLugar, ubicacion, fechaInicio, fechaFin, imgUrl, tags)
            )
            Result.success(response["success"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExhibition(id: Long): Result<Boolean> {
        return try {
            val response = api.deleteExhibition(id)
            Result.success(response["success"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArtworkDetail(id: Long): Result<ArtworkDetail> {
        return try {
            val dto = api.getArtworkDetail(id)
            Result.success(
                ArtworkDetail(
                    idObra = dto.idObra,
                    titulo = dto.titulo,
                    descrip = dto.descrip,
                    imgUrl = dto.imgUrl,
                    precio = dto.precio,
                    disponibleCompra = dto.disponibleCompra,
                    tags = dto.tags,
                    categoriasExposicion = dto.categoriasExposicion
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateArtwork(
        id: Long, titulo: String, descrip: String?, precio: Double?, disponibleCompra: Boolean, tags: List<String>
    ): Result<Boolean> {
        return try {
            val response = api.updateArtwork(
                id, com.antigravity.swart.data.remote.dto.UpdateArtworkRequest(titulo, descrip, precio, disponibleCompra, tags)
            )
            Result.success(response["success"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteArtwork(id: Long): Result<Boolean> {
        return try {
            val response = api.deleteArtwork(id)
            Result.success(response["success"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyAddress(address: String): Result<com.antigravity.swart.domain.model.GeocodingResult> {
        return try {
            val dto = api.verifyAddress(address)
            Result.success(
                com.antigravity.swart.domain.model.GeocodingResult(
                    lat = dto.lat.toDoubleOrNull() ?: 0.0,
                    lon = dto.lon.toDoubleOrNull() ?: 0.0,
                    displayName = dto.display_name
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createExhibition(request: com.antigravity.swart.data.remote.dto.CreateExhibitionRequest): Result<Long> {
        return try {
            val response = api.createExhibition(request)
            Result.success(response["idExposicion"] ?: -1L)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createArtwork(request: com.antigravity.swart.data.remote.dto.CreateArtworkRequest): Result<Long> {
        return try {
            val response = api.createArtwork(request)
            Result.success(response["idObra"] ?: -1L)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMutuals(artistId: Long): Result<List<com.antigravity.swart.data.remote.dto.MutualArtistDto>> {
        return try {
            Result.success(api.getMutuals(artistId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInvitations(userId: Long): Result<List<com.antigravity.swart.data.remote.dto.InvitationDto>> {
        return try {
            Result.success(api.getInvitations(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun respondInvitation(invitationId: Long, accept: Boolean): Result<Boolean> {
        return try {
            val response = api.respondInvitation(invitationId, mapOf("aceptar" to accept))
            Result.success(response["success"] ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArtists(userId: Long): Result<List<com.antigravity.swart.data.remote.dto.ArtistFollowDto>> {
        return try {
            Result.success(api.getArtists(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(filePart: okhttp3.MultipartBody.Part): Result<String> {
        return try {
            val response = api.uploadImage(filePart)
            val url = response["url"] ?: throw Exception("URL no devuelta por el servidor")
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startChat(senderId: Long, receiverId: Long, initialMessage: String, urlImagenObra: String?): Result<Long> {
        return try {
            val req = com.antigravity.swart.data.remote.dto.StartChatRequest(senderId, receiverId, initialMessage, urlImagenObra)
            val response = api.startChat(req)
            Result.success(response["idConversacion"] ?: -1L)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversations(userId: Long): Result<List<com.antigravity.swart.domain.model.Conversation>> {
        return try {
            val response = api.getConversations(userId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatMessages(chatId: Long): Result<List<com.antigravity.swart.domain.model.Message>> {
        return try {
            val response = api.getChatMessages(chatId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendChatMessage(chatId: Long, senderId: Long, contenido: String): Result<Boolean> {
        return try {
            val req = com.antigravity.swart.data.remote.dto.SendMessageRequest(senderId, contenido)
            val response = api.sendChatMessage(chatId, req)
            Result.success(response.containsKey("idMensaje") || response.containsKey("fechaCreacion"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
