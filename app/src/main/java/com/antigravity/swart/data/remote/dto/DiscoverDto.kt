package com.antigravity.swart.data.remote.dto

data class DiscoverArtworkDto(
    val idObra: Long,
    val titulo: String,
    val imgUrl: String,
    val matchScore: Double,
    val isExploration: Boolean,
    val exhibitionId: Long,
    val exhibitionTitle: String,
    val nombreLugar: String?,
    val ubicacion: String?,
    val artistName: String,
    val artistAvatar: String,
    val artistId: Long = 0L
)

data class SwipeRequestDto(
    val idInteresado: Long,
    val idObra: Long,
    val liked: Boolean,
    val matchScore: Double
)

fun DiscoverArtworkDto.toDomain() = com.antigravity.swart.domain.model.DiscoverArtwork(
    id = idObra,
    title = titulo,
    imageUrl = imgUrl,
    matchScore = matchScore,
    isExploration = isExploration,
    exhibitionId = exhibitionId,
    exhibitionTitle = exhibitionTitle,
    locationName = nombreLugar,
    locationAddress = ubicacion,
    artistName = artistName,
    artistAvatarUrl = artistAvatar,
    artistId = artistId
)
