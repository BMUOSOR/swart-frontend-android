package com.antigravity.swart.data.remote.dto

data class PropuestaRequest(
    val idArtista: Long,
    val titulo: String,
    val descrip: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val precio: Double? = null,
    val categoria: String? = null,
    val archivoPdf: String? = null   // URL del PDF en Supabase Storage (opcional)
)

data class PropuestaEstadoRequest(
    val estado: String
)
