package com.antigravity.swart.data.remote.dto

data class InvitationDto(
    val idInvitacion: Long,
    val idExposicion: Long,
    val tituloExposicion: String,
    val exposicionImgUrl: String?,
    val idArtistaSender: Long,
    val nombreArtistaSender: String,
    val avatarArtistaSender: String?,
    val estado: String,
    val tipo: String = "invitacion",
    // Campos de detalle de propuesta (null para invitaciones normales)
    val descrip: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val precio: Double? = null,
    val categoria: String? = null,
    val idUsuarioSender: Long? = null,
    val nombreEspacio: String? = null    // título de la baliza vacía
)

data class MutualArtistDto(
    val id: Long,
    val nombre: String,
    val avatarUrl: String?
)

data class ArtistFollowDto(
    val id: Long,
    val nombre: String,
    val avatarUrl: String?,
    val following: Boolean
)
