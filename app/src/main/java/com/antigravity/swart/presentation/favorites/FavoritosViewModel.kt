package com.antigravity.swart.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.DiscoverArtwork
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritosUiState(
    val isLoading: Boolean = false,
    val artworks: List<DiscoverArtwork> = emptyList(),
    val error: String? = null,
    val chatLoading: Boolean = false
)

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        loadFavoritos()
    }

    fun loadFavoritos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = sessionManager.getUserId()
            repository.getLikedArtworks(userId).fold(
                onSuccess = { artworks ->
                    _uiState.value = FavoritosUiState(isLoading = false, artworks = artworks)
                },
                onFailure = { e ->
                    _uiState.value = FavoritosUiState(isLoading = false, error = e.message)
                }
            )
        }
    }

    /** Elimina la obra de favoritos en el servidor y la quita del estado local. */
    fun removeLike(obraId: Long) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            // Optimistic update: quitar de la lista inmediatamente
            _uiState.value = _uiState.value.copy(
                artworks = _uiState.value.artworks.filter { it.id != obraId }
            )
            repository.removeLike(userId, obraId).onFailure {
                // Si falla, recargar para mostrar el estado real
                loadFavoritos()
            }
        }
    }

    /** Abre (o crea) un chat con el autor de la obra y devuelve el chatId. */
    fun startChat(artwork: DiscoverArtwork, message: String, onChatCreated: (Long) -> Unit) {
        val senderId = sessionManager.getUserId()
        if (senderId == -1L || artwork.artistId == 0L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(chatLoading = true)
            repository.startChat(
                senderId = senderId,
                receiverId = artwork.artistId,
                initialMessage = message,
                urlImagenObra = artwork.imageUrl
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
}
