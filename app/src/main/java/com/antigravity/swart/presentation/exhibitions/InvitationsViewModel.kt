package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.data.remote.dto.InvitationDto
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationsViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Invitaciones a exposiciones colaborativas (tipo == "invitacion")
    private val _invitations = MutableStateFlow<List<InvitationDto>>(emptyList())
    val invitations: StateFlow<List<InvitationDto>> = _invitations.asStateFlow()

    // Propuestas recibidas para balizas vacías (tipo == "propuesta")
    private val _propuestasEspacios = MutableStateFlow<List<InvitationDto>>(emptyList())
    val propuestasEspacios: StateFlow<List<InvitationDto>> = _propuestasEspacios.asStateFlow()

    private val _conversations = MutableStateFlow<List<com.antigravity.swart.domain.model.Conversation>>(emptyList())
    val conversations: StateFlow<List<com.antigravity.swart.domain.model.Conversation>> = _conversations.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = sessionManager.getUserId()
            if (userId != -1L) {
                repository.getInvitations(userId).fold(
                    onSuccess = { all ->
                        _invitations.value = all.filter { it.tipo != "propuesta" }
                        _propuestasEspacios.value = all.filter { it.tipo == "propuesta" }
                    },
                    onFailure = { }
                )
                repository.getConversations(userId).fold(
                    onSuccess = { _conversations.value = it },
                    onFailure = { }
                )
                repository.getUnreadCount(userId).fold(
                    onSuccess = { _unreadCount.value = it },
                    onFailure = { }
                )
            }
            _isLoading.value = false
        }
    }

    fun respondInvitation(id: Long, accept: Boolean, tipo: String = "invitacion") {
        viewModelScope.launch {
            if (tipo == "propuesta") {
                val estado = if (accept) "aceptada" else "rechazada"
                repository.respondPropuesta(id, estado)
            } else {
                repository.respondInvitation(id, accept)
            }
            loadData()
        }
    }

    fun startChatWithSender(senderUserId: Long, onChatCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val myId = sessionManager.getUserId()
            repository.startChat(senderId = myId, receiverId = senderUserId).fold(
                onSuccess = { chatId -> onChatCreated(chatId) },
                onFailure = { }
            )
        }
    }
}
