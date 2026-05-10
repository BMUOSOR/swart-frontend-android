package com.antigravity.swart.domain.model

data class Exhibition(
    val id: Long,
    val title: String,
    val description: String?,
    val artistName: String,
    val artistAvatarUrl: String,
    val isNew: Boolean,
    val artworksCount: Int,
    val artworkImagesUrls: List<String>,
    val exhibitionImgUrl: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val nombreLugar: String?,
    val ubicacion: String?,
    val precio: Double?,
    val score: Double?,
    val tags: List<String>
)
