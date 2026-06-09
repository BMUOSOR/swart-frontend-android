package com.antigravity.swart.data.remote.dto

data class StartChatRequest(
    val senderId: Long,
    val receiverId: Long,
    val initialMessage: String? = null,
    val urlImagenObra: String? = null
)

data class ConversationDto(
    val idConversacion: Long,
    val otherUserId: Long,
    val otherUserNombre: String,
    val otherUserAvatar: String?,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)

data class MessageDto(
    val idMensaje: Long,
    val idSender: Long,
    val contenido: String,
    val urlImagenObra: String?,
    val fechaCreacion: String
)

data class SendMessageRequest(
    val senderId: Long,
    val contenido: String
)

fun ConversationDto.toDomain() = com.antigravity.swart.domain.model.Conversation(
    idConversacion = idConversacion,
    otherUserId = otherUserId,
    otherUserNombre = otherUserNombre,
    otherUserAvatar = otherUserAvatar,
    ultimoMensaje = ultimoMensaje,
    fechaUltimoMensaje = fechaUltimoMensaje
)

fun MessageDto.toDomain() = com.antigravity.swart.domain.model.Message(
    idMensaje = idMensaje,
    idSender = idSender,
    contenido = contenido,
    urlImagenObra = urlImagenObra,
    fechaCreacion = fechaCreacion
)
