package com.antigravity.swart.data.remote.dto

data class ArtistFeedDto(
    val id: Long,
    val nombre: String,
    val avatarUrl: String
)

data class ExhibitionFeedDto(
    val idExposicion: Long,
    val idArtista: Long,
    val titulo: String,
    val descrip: String?,
    val artistaNombre: String,
    val artistaAvatar: String,
    val artistas: List<ArtistFeedDto>? = emptyList(),
    val isNew: Boolean,
    val obrasCount: Int,
    val obras: List<ArtworkDto>,
    val exposicionImgUrl: String?,
    val tags: List<String>,
    val fechaInicio: String?,
    val fechaFin: String?,
    val nombreLugar: String?,
    val ubicacion: String?,
    val precio: Double?,
    val score: Double?
)
