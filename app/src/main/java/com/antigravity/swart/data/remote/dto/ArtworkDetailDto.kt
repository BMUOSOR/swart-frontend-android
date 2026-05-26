package com.antigravity.swart.data.remote.dto

data class ArtworkDetailDto(
    val idObra: Long,
    val titulo: String,
    val descrip: String?,
    val imgUrl: String?,
    val precio: Double?,
    val disponibleCompra: Boolean,
    val tags: List<String>,
    val categoriasExposicion: List<String>
)

data class UpdateArtworkRequest(
    val titulo: String,
    val descrip: String?,
    val precio: Double?,
    val disponibleCompra: Boolean,
    val tags: List<String>
)
