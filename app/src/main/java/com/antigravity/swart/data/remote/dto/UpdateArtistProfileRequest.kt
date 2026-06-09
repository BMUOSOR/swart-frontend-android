package com.antigravity.swart.data.remote.dto

data class UpdateArtistProfileRequest(
    val bio: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val correo: String? = null
)
