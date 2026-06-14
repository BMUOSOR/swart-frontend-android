package com.antigravity.swart.data.remote.dto

data class MapPinDto(
    val idExposicion: Long,
    val lat: Double,
    val lon: Double,
    val titulo: String,
    val galeria: String,
    val imagen: String?,
    val distancia: String,
    val match: Int,
    val mainTag: String,
    val startDate: Long? = null,
    val endDate: Long? = null
)

data class GovBalizaDto(
    val id: Long,
    val nombre: String,
    val direccion: String,
    val telefono: String?,
    val email: String?,
    val lat: Double,
    val lon: Double
)

data class PropuestaDto(
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
    val fechaCreacion: String
)
