package com.antigravity.swart.data.remote.dto

data class ExhibitionDetailDto(
    val idExposicion: Long,
    val titulo: String,
    val descrip: String?,
    val nombreLugar: String?,
    val ubicacion: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val imgUrl: String?,
    val precio: Double?,
    val score: Double?,
    val activa: Boolean,
    val obras: List<ArtworkDto>,
    val tags: List<String>,
    val esColaborativa: Boolean = false,
    val artistas: List<ArtistFeedDto> = emptyList()
)

data class UpdateExhibitionRequest(
    val titulo: String,
    val descrip: String?,
    val nombreLugar: String?,
    val ubicacion: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val imgUrl: String?,
    val tags: List<String>
)
