package com.antigravity.swart.data.remote.dto

import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.model.ArtworkForSale

data class ArtistProfileDto(
    val id: Long,
    val nombre: String,
    val apellidos: String?,
    val avatarUrl: String?,
    val bio: String?,
    val seguidores: Int,
    val exposicionesCount: Int,
    val instagram: String?,
    val twitter: String?,
    val correo: String?,
    val activeExhibitions: List<ExhibitionFeedDto>,
    val worksForSale: List<ArtworkForSaleDto>
)

data class ArtworkForSaleDto(
    val idObra: Long,
    val titulo: String,
    val imgUrl: String,
    val precio: Double
)

fun ArtistProfileDto.toDomain(): ArtistProfile {
    return ArtistProfile(
        id = id,
        nombre = nombre,
        apellidos = apellidos,
        avatarUrl = avatarUrl,
        bio = bio,
        seguidores = seguidores,
        exposicionesCount = exposicionesCount,
        instagram = instagram,
        twitter = twitter,
        correo = correo,
        activeExhibitions = activeExhibitions.map { it.toDomain() },
        worksForSale = worksForSale.map { it.toDomain() }
    )
}

fun ArtworkForSaleDto.toDomain(): ArtworkForSale {
    return ArtworkForSale(
        idObra = idObra,
        titulo = titulo,
        imgUrl = imgUrl,
        precio = precio
    )
}
