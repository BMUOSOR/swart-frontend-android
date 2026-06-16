package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.EmptyBaliza

data class EmptyBalizaDto(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val idPropietario: Long
)

data class EmptyBalizaRequest(
    val lat: Double,
    val lon: Double,
    val idPropietario: Long
)

fun EmptyBalizaDto.toDomain(): EmptyBaliza {
    return EmptyBaliza(id = id, lat = lat, lon = lon, idPropietario = idPropietario)
}

data class BalizaVaciaDetailDto(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val idPropietario: Long,
    val nombrePropietario: String,
    val avatarPropietario: String?,
    val titulo: String?,
    val descripcion: String?,
    val categorias: String?,       // CSV: "Pintura,Escultura"
    val dimensiones: String?,
    val plantas: String?,
    val salas: String?,            // JSON array
    val fotos: String?             // JSON array of URLs
)

data class UpdateBalizaVaciaRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categorias: String? = null,
    val dimensiones: String? = null,
    val plantas: String? = null,
    val salas: String? = null,
    val fotos: String? = null
)
