package com.antigravity.swart.presentation.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.model.ArtworkForSale
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistProfileViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Recuperamos el artistId de los argumentos de navegación (el artista que se está viendo)
    private val artistId: Long = try {
        val arg = savedStateHandle.get<String>("artistId")
        arg?.toLongOrNull() ?: savedStateHandle.get<Long>("artistId") ?: 1L
    } catch (e: Exception) {
        1L
    }

    // El userId del usuario autenticado (el que hace follow)
    private val currentUserId: Long = sessionManager.getUserId()

    val isOwnProfile: Boolean = artistId == currentUserId

    private val _uiState = MutableStateFlow<ArtistProfileUiState>(ArtistProfileUiState.Loading)
    val uiState: StateFlow<ArtistProfileUiState> = _uiState.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    init {
        loadArtistProfile()
    }

    fun loadArtistProfile() {
        viewModelScope.launch {
            _uiState.value = ArtistProfileUiState.Loading
            repository.getArtistProfile(artistId).fold(
                onSuccess = { profile ->
                    _uiState.value = ArtistProfileUiState.Success(profile)
                    // Carga el estado real de follow si hay sesión activa
                    if (currentUserId != -1L) {
                        repository.getFollowStatus(artistId, currentUserId).fold(
                            onSuccess = { isFollowing -> _isFollowing.value = isFollowing },
                            onFailure = { _isFollowing.value = profile.seguidores % 2 != 0 }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = ArtistProfileUiState.Error(error.message ?: "Error de conexión")
                }
            )
        }
    }

    fun toggleFollow() {
        if (isOwnProfile) return
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ArtistProfileUiState.Success) {
                val currentProfile = currentState.profile
                val userId = if (currentUserId != -1L) currentUserId else 1L
                repository.followArtist(artistId, userId).fold(
                    onSuccess = { following ->
                        _isFollowing.value = following
                        val updatedProfile = currentProfile.copy(
                            seguidores = if (following) currentProfile.seguidores + 1 else maxOf(0, currentProfile.seguidores - 1)
                        )
                        _uiState.value = ArtistProfileUiState.Success(updatedProfile)
                    },
                    onFailure = {
                        // Feedback silencioso para conservar estabilidad
                    }
                )
            }
        }
    }

    fun startInquiryChat(work: ArtworkForSale, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            if (currentUserId == -1L) return@launch
            repository.startChat(
                senderId = currentUserId,
                receiverId = artistId,
                initialMessage = "Hola! Me ha interesado esta obra, ¿Podemos hablar sobre ella?.",
                urlImagenObra = work.imgUrl
            ).fold(
                onSuccess = { chatId ->
                    onSuccess(chatId)
                },
                onFailure = { }
            )
        }
    }

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    fun updateProfile(bio: String?, instagram: String?, twitter: String?, correo: String?) {
        viewModelScope.launch {
            _isSavingProfile.value = true
            repository.updateArtistProfile(artistId, bio, instagram, twitter, correo).fold(
                onSuccess = {
                    _isSavingProfile.value = false
                    // Refrescar el perfil para que la UI refleje los cambios
                    loadArtistProfile()
                },
                onFailure = {
                    _isSavingProfile.value = false
                }
            )
        }
    }

    fun startGeneralChat(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            if (currentUserId == -1L) return@launch
            repository.startChat(
                senderId = currentUserId,
                receiverId = artistId
            ).fold(
                onSuccess = { chatId ->
                    onSuccess(chatId)
                },
                onFailure = { }
            )
        }
    }
}

sealed interface ArtistProfileUiState {
    object Loading : ArtistProfileUiState
    data class Success(val profile: ArtistProfile) : ArtistProfileUiState
    data class Error(val message: String) : ArtistProfileUiState
}
