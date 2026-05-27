package com.antigravity.swart.data.remote.dto

data class CreateExhibitionRequest(
    val titulo: String,
    val descrip: String? = null,
    val nombreLugar: String? = null,
    val ubicacion: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val imgUrl: String? = null,
    val precio: Double? = null,
    val activa: Boolean = true,
    val esColaborativa: Boolean = false,
    val artistaId: Long,
    val artistasInvitadosIds: List<Long> = emptyList()
)
