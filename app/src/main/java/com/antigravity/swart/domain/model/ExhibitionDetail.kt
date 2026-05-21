package com.antigravity.swart.domain.model

data class ExhibitionDetail(
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
    val obras: List<Artwork>,
    val tags: List<String>
)
