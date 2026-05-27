package com.antigravity.swart.data.remote.dto

data class CreateArtworkRequest(
    val idExposicion: Long,
    val titulo: String,
    val descrip: String? = null,
    val imgUrl: String? = null,
    val precio: Double? = null,
    val disponibleCompra: Boolean = false,
    val tags: List<String> = emptyList()
)
