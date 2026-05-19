package com.antigravity.swart.domain.model

data class ArtistProfile(
    val id: Long,
    val nombre: String,
    val apellidos: String?,
    val avatarUrl: String?,
    val bio: String?,
    val seguidores: Int,
    val exposicionesCount: Int,
    val instagram: String?,
    val twitter: String?, // x
    val correo: String?,
    val activeExhibitions: List<Exhibition>,
    val worksForSale: List<ArtworkForSale>
)

data class ArtworkForSale(
    val idObra: Long,
    val titulo: String,
    val imgUrl: String,
    val precio: Double
)
