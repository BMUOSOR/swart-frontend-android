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
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    // En un caso real, obtendriamos el userId del AuthRepository.
    // Como acordamos, haremos fallback a Marina Soto (ID 3) para testear sin login.
    private val currentUserId: Long = 3L 

    init {
        loadFeed()
    }

    private fun loadFeed() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getDiscoverFeed(currentUserId).fold(
                onSuccess = { artworks ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        artworks = artworks
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar el feed de descubrimiento"
                    )
                }
            )
        }
    }

    fun recordSwipe(artwork: DiscoverArtwork, liked: Boolean) {
        viewModelScope.launch {
            repository.recordSwipe(currentUserId, artwork.id, liked, artwork.matchScore)
            // No bloqueamos UI. El error se podría manejar aquí.
        }
    }
}
