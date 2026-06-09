package com.antigravity.swart.presentation.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.domain.model.DiscoverArtwork
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val artworks: List<DiscoverArtwork> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: com.antigravity.swart.core.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val currentUserId: Long
        get() = sessionManager.getUserId()

    /** IDs ya likeados: nunca vuelven a aparecer en el mazo aunque el backend los devuelva. */
    private val localLikedIds = mutableSetOf<Long>()

    init {
        loadFeed()
    }

    fun loadFeed() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getDiscoverFeed(currentUserId).fold(
                onSuccess = { artworks ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        artworks = artworks.filter { it.id !in localLikedIds }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar el feed"
                    )
                }
            )
        }
    }

    private fun loadMoreFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            repository.getDiscoverFeed(currentUserId).fold(
                onSuccess = { newArtworks ->
                    val currentIds = _uiState.value.artworks.map { it.id }.toSet()
                    val uniqueNew = newArtworks.filter {
                        it.id !in currentIds && it.id !in localLikedIds
                    }
                    if (uniqueNew.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            artworks = _uiState.value.artworks + uniqueNew
                        )
                    }
                },
                onFailure = { /* silent */ }
            )
        }
    }

    /**
     * Registra el swipe:
     *  1. Elimina la obra de la lista de inmediato (el composable siempre mostrará artworks[0]).
     *  2. Si es like, la añade a localLikedIds para que no reaparezca.
     *  3. Envía el evento al servidor.
     *  4. Si la lista queda pequeña, carga más en background.
     */
    suspend fun recordSwipe(artwork: DiscoverArtwork, liked: Boolean) {
        // 1. Eliminar de la lista local ANTES de esperar al servidor
        _uiState.value = _uiState.value.copy(
            artworks = _uiState.value.artworks.filter { it.id != artwork.id }
        )
        if (liked) localLikedIds.add(artwork.id)

        // 2. Registrar en el servidor (await para que loadMore no devuelva la misma obra)
        repository.recordSwipe(currentUserId, artwork.id, liked, artwork.matchScore).fold(
            onSuccess = {
                android.util.Log.d("DiscoverVM", "Swipe OK: ${artwork.id} liked=$liked")
            },
            onFailure = { e ->
                android.util.Log.e("DiscoverVM", "Swipe failed: ${e.message}")
                // Si el servidor falló, revertir el filtro local (no el like visual)
                if (liked) localLikedIds.remove(artwork.id)
            }
        )

        // 3. Si quedan pocas obras, cargar más
        if (_uiState.value.artworks.size <= 2) {
            loadMoreFeed()
        }
    }
}
