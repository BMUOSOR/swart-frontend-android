package com.antigravity.swart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val nombre: String = "",
    val apellidos: String = "",
    val imgUrl: String = "",
    val location: String = "Madrid, España",
    val role: String = "",
    val pendingInvitationsCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = sessionManager.getUserId()
        val role = sessionManager.getRole()
        val nombre = sessionManager.getNombre()
        val apellidos = sessionManager.getApellidos()
        val imgUrl = sessionManager.getImgUrl().ifBlank {
            "https://bkrmqkpxidmemzxhefoc.supabase.co/storage/v1/object/public/Imagenes/usuario_chica_3.jpg"
        }

        _uiState.value = UserProfileUiState(
            nombre = nombre,
            apellidos = apellidos,
            imgUrl = imgUrl,
            role = role,
            isLoading = true
        )

        if (userId != -1L) {
            viewModelScope.launch {
                repository.getInvitations(userId).fold(
                    onSuccess = { invitations ->
                        _uiState.value = _uiState.value.copy(
                            pendingInvitationsCount = invitations.size,
                            isLoading = false
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
