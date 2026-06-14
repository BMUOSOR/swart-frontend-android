package com.antigravity.swart.domain.model

data class GovBaliza(
    val id: Long,
    val nombre: String,
    val direccion: String,
    val telefono: String?,
    val email: String?,
    val lat: Double,
    val lon: Double
)
