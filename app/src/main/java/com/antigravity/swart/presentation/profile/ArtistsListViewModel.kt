package com.antigravity.swart.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.data.remote.dto.ArtistFollowDto
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistsListUiState(
    val artists: List<ArtistFollowDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chatLoading: Boolean = false
)

@HiltViewModel
class ArtistsListViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistsListUiState())
    val uiState: StateFlow<ArtistsListUiState> = _uiState

    init {
        loadArtists()
    }

    fun loadArtists() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getArtists(userId).fold(
                onSuccess = { artists ->
                    // Exclude self from the list
                    val filteredArtists = artists.filter { it.id != userId }
                    _uiState.value = ArtistsListUiState(
                        artists = filteredArtists,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar artistas"
                    )
                }
            )
        }
    }

    fun startChat(artistId: Long, onChatCreated: (Long) -> Unit) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return
        _uiState.value = _uiState.value.copy(chatLoading = true)
        viewModelScope.launch {
            repository.startChat(
                senderId = userId,
                receiverId = artistId
            ).fold(
                onSuccess = { chatId ->
                    _uiState.value = _uiState.value.copy(chatLoading = false)
                    onChatCreated(chatId)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(chatLoading = false)
                }
            )
        }
    }

    fun toggleFollow(artistId: Long) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewModelScope.launch {
            repository.followArtist(artistId, userId).fold(
                onSuccess = { isFollowing ->
                    // Update state locally
                    val updatedList = _uiState.value.artists.map { artist ->
                        if (artist.id == artistId) {
                            artist.copy(following = isFollowing)
                        } else {
                            artist
                        }
                    }
                    _uiState.value = _uiState.value.copy(artists = updatedList)
                },
                onFailure = { /* Opcional: mostrar error */ }
            )
        }
    }
}
