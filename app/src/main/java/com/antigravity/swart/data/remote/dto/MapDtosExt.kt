package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.model.GovBaliza
import com.antigravity.swart.domain.model.PropuestaBaliza

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

fun GovBalizaDto.toDomain(): GovBaliza {
    return GovBaliza(
        id = id,
        nombre = nombre,
        direccion = direccion,
        telefono = telefono,
        email = email,
        lat = lat,
        lon = lon
    )
}

fun PropuestaDto.toDomain(): PropuestaBaliza {
    return PropuestaBaliza(
        id = id,
        idBaliza = idBaliza,
        idArtista = idArtista,
        titulo = titulo,
        descrip = descrip,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        precio = precio,
        categoria = categoria,
        estado = estado,
        fechaCreacion = fechaCreacion
    )
}
