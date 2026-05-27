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

    private val _invitations = MutableStateFlow<List<InvitationDto>>(emptyList())
    val invitations: StateFlow<List<InvitationDto>> = _invitations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadInvitations() }

    fun loadInvitations() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = sessionManager.getUserId()
            repository.getInvitations(userId).fold(
                onSuccess = { _invitations.value = it },
                onFailure = { }
            )
            _isLoading.value = false
        }
    }

    fun respondInvitation(invitationId: Long, accept: Boolean) {
        viewModelScope.launch {
            repository.respondInvitation(invitationId, accept).fold(
                onSuccess = { loadInvitations() },
                onFailure = { }
            )
        }
    }
}
