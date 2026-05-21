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
                    obras = dto.obras.map { Artwork(it.idObra, it.titulo, it.imgUrl) },
                    tags = dto.tags
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExhibition(
        id: Long, titulo: String, descrip: String?, nombreLugar: String?,
        ubicacion: String?, fechaInicio: String?, fechaFin: String?, tags: List<String>
    ): Result<Boolean> {
        return try {
            val response = api.updateExhibition(
                id, UpdateExhibitionRequest(titulo, descrip, nombreLugar, ubicacion, fechaInicio, fechaFin, tags)
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
}
