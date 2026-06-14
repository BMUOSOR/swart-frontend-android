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
