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
            repository.recordSwipe(currentUserId, artwork.id, liked, artwork.matchScore).fold(
                onSuccess = {
                    android.util.Log.d("DiscoverVM", "Swipe recorded successfully for artwork ${artwork.id}")
                },
                onFailure = { error ->
                    android.util.Log.e("DiscoverVM", "Failed to record swipe: ${error.message}", error)
                }
            )
        }
    }
}


