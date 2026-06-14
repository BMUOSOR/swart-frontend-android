package com.antigravity.swart.data.remote.dto

data class PropuestaRequest(
    val idArtista: Long,
    val titulo: String,
    val descrip: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val precio: Double? = null,
    val categoria: String? = null
)

data class PropuestaEstadoRequest(
    val estado: String
)
