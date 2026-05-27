package com.antigravity.swart.data.remote.dto

data class ArtworkDto(
    val idObra: Long,
    val idArtista: Long? = null,
    val titulo: String,
    val imgUrl: String
)
