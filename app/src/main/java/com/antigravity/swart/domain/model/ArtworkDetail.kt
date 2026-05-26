package com.antigravity.swart.domain.model

data class ArtworkDetail(
    val idObra: Long,
    val titulo: String,
    val descrip: String?,
    val imgUrl: String?,
    val precio: Double?,
    val disponibleCompra: Boolean,
    val tags: List<String>,
    val categoriasExposicion: List<String>
)
