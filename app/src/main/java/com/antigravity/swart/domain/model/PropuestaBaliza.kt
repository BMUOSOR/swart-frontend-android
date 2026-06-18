package com.antigravity.swart.domain.model

data class PropuestaBaliza(
    val id: Long,
    val idBaliza: Long,
    val idArtista: Long,
    val titulo: String,
    val descrip: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val precio: Double?,
    val categoria: String?,
    val estado: String,
    val fechaCreacion: String,
    val archivoPdf: String? = null
)
