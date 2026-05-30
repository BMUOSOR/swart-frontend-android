package com.antigravity.swart.domain.model

data class Conversation(
    val idConversacion: Long,
    val otherUserId: Long,
    val otherUserNombre: String,
    val otherUserAvatar: String?,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)

data class Message(
    val idMensaje: Long,
    val idSender: Long,
    val contenido: String,
    val urlImagenObra: String?,
    val fechaCreacion: String
)
