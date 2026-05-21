package com.antigravity.swart.domain.model

data class ArtistBasic(
    val id: Long,
    val name: String,
    val avatarUrl: String
)

data class Exhibition(
    val id: Long,
    val artistId: Long,
    val title: String,
    val description: String?,
    val artistName: String,
    val artistAvatarUrl: String,
    val artists: List<ArtistBasic> = emptyList(),
    val isNew: Boolean,
    val artworksCount: Int,
    val artworks: List<Artwork>,
    val exhibitionImgUrl: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val nombreLugar: String?,
    val ubicacion: String?,
    val precio: Double?,
    val score: Double?,
    val tags: List<String>
)

data class Artwork(
    val id: Long,
    val title: String,
    val imageUrl: String
)

data class DiscoverArtwork(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val matchScore: Double,
    val isExploration: Boolean,
    val exhibitionId: Long,
    val exhibitionTitle: String,
    val locationName: String?,
    val locationAddress: String?,
    val artistName: String,
    val artistAvatarUrl: String
)
