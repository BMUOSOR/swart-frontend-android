package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.EmptyBaliza

data class EmptyBalizaDto(val id: Long, val lat: Double, val lon: Double)
data class EmptyBalizaRequest(val lat: Double, val lon: Double)

fun EmptyBalizaDto.toDomain() = EmptyBaliza(id = id, lat = lat, lon = lon)
