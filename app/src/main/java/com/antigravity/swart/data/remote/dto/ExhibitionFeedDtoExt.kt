package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.model.Artwork
import com.antigravity.swart.domain.model.ArtistBasic

fun ExhibitionFeedDto.toDomain(): Exhibition {
    return Exhibition(
        id = idExposicion,
        artistId = idArtista,
        title = titulo,
        description = descrip,
        artistName = artistaNombre,
        artistAvatarUrl = artistaAvatar,
        artists = artistas?.map { ArtistBasic(it.id, it.nombre, it.avatarUrl) } ?: listOf(
            ArtistBasic(idArtista, artistaNombre, artistaAvatar)
        ),
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
