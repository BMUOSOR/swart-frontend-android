package com.antigravity.swart.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.Message
import com.antigravity.swart.domain.model.Conversation
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val chatId: Long = try {
        val arg = savedStateHandle.get<String>("chatId")
        arg?.toLongOrNull() ?: savedStateHandle.get<Long>("chatId") ?: 1L
    } catch (e: Exception) {
        1L
    }

    val currentUserId: Long = sessionManager.getUserId()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _otherUserNombre = MutableStateFlow("Chat")
    val otherUserNombre: StateFlow<String> = _otherUserNombre.asStateFlow()

    private val _otherUserAvatar = MutableStateFlow<String?>(null)
    val otherUserAvatar: StateFlow<String?> = _otherUserAvatar.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var isPollingActive = true

    init {
        loadChatDetails()
        startMessagePolling()
    }

    private fun loadChatDetails() {
        viewModelScope.launch {
            if (currentUserId == -1L) return@launch
            repository.getConversations(currentUserId).fold(
                onSuccess = { list ->
                    val match = list.find { it.idConversacion == chatId }
                    if (match != null) {
                        _otherUserNombre.value = match.otherUserNombre
                        _otherUserAvatar.value = match.otherUserAvatar
                    }
                },
                onFailure = { }
            )
        }
    }

    private fun startMessagePolling() {
        viewModelScope.launch {
            _isLoading.value = true
            var isFirstLoad = true
            while (isPollingActive) {
                repository.getChatMessages(chatId).fold(
                    onSuccess = { list ->
                        _messages.value = list
                        if (isFirstLoad) {
                            _isLoading.value = false
                            isFirstLoad = false
                        }
                    },
                    onFailure = {
                        if (isFirstLoad) {
                            _isLoading.value = false
                            isFirstLoad = false
                        }
                    }
                )
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || currentUserId == -1L) return
        viewModelScope.launch {
            repository.sendChatMessage(chatId, currentUserId, content).fold(
                onSuccess = {
                    // Force refresh immediately
                    repository.getChatMessages(chatId).fold(
                        onSuccess = { _messages.value = it },
                        onFailure = { }
                    )
                },
                onFailure = { }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPollingActive = false
    }
}
