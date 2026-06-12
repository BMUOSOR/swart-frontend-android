package com.antigravity.swart.data.remote.dto

data class MapPinDto(
    val idExposicion: Long,
    val lat: Double,
    val lon: Double,
    val titulo: String,
    val galeria: String,
    val imagen: String?,
    val distancia: String,
    val match: Int,
    val mainTag: String,
    val startDate: Long? = null,
    val endDate: Long? = null
)
