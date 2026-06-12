package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.MapPin

fun MapPinDto.toDomain(): MapPin {
    return MapPin(
        idExposicion = idExposicion,
        lat = lat,
        lon = lon,
        titulo = titulo,
        galeria = galeria,
        imagen = imagen,
        distancia = distancia,
        match = match,
        mainTag = mainTag,
        startDate = startDate,
        endDate = endDate
    )
}
