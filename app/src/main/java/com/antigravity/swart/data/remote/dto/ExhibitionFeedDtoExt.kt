package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.Artwork

fun ExhibitionFeedDto.toDomain(): Exhibition {
    return Exhibition(
        id = idExposicion,
        title = titulo,
        description = descrip,
        artistName = artistaNombre,
        artistAvatarUrl = artistaAvatar,
        isNew = isNew,
        artworksCount = obrasCount,
        artworks = obras.map { Artwork(it.idObra, it.titulo, it.imgUrl) },
        exhibitionImgUrl = exposicionImgUrl,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        nombreLugar = nombreLugar,
        ubicacion = ubicacion,
        precio = precio,
        score = score,
        tags = tags
    )
}
