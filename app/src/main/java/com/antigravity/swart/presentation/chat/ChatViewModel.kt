package com.antigravity.swart.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.Message
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    // Prefer savedStateHandle value; screen calls ensureChatId() to override if needed
    private val _chatId = MutableStateFlow(
        savedStateHandle.get<Long>("chatId") ?: 0L
    )
    val chatId: Long get() = _chatId.value

    val currentUserId: Long = sessionManager.getUserId()
    val currentUserRole: String = sessionManager.getRole()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _otherUserNombre = MutableStateFlow("Chat")
    val otherUserNombre: StateFlow<String> = _otherUserNombre.asStateFlow()

    private val _otherUserAvatar = MutableStateFlow<String?>(null)
    val otherUserAvatar: StateFlow<String?> = _otherUserAvatar.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            _chatId.collect { id ->
                if (id > 0L) {
                    loadChatDetails(id)
                    startMessagePolling(id)
                }
            }
        }
    }

    // Called by ChatScreen to guarantee the correct chatId is used regardless of
    // whether SavedStateHandle was populated before the ViewModel was created.
    fun ensureChatId(id: Long) {
        if (id > 0L && _chatId.value != id) {
            _chatId.value = id
        }
    }

    private fun loadChatDetails(id: Long) {
        viewModelScope.launch {
            if (currentUserId == -1L) return@launch
            repository.getConversations(currentUserId).fold(
                onSuccess = { list ->
                    val match = list.find { it.idConversacion == id }
                    if (match != null) {
                        _otherUserNombre.value = match.otherUserNombre
                        _otherUserAvatar.value = match.otherUserAvatar
                    }
                },
                onFailure = { }
            )
        }
    }

    private fun startMessagePolling(id: Long) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _isLoading.value = true
            var isFirstLoad = true
            while (true) {
                repository.getChatMessages(id).fold(
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
                delay(3000)
            }
        }
    }

    fun sendMessage(content: String) {
        val id = _chatId.value
        if (content.isBlank() || currentUserId == -1L || id <= 0L) return
        viewModelScope.launch {
            repository.sendChatMessage(id, currentUserId, content).fold(
                onSuccess = {
                    repository.getChatMessages(id).fold(
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
        pollingJob?.cancel()
    }
}
