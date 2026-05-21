package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.ArtistProfile
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyExhibitionsViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyExhibitionsUiState>(MyExhibitionsUiState.Loading)
    val uiState: StateFlow<MyExhibitionsUiState> = _uiState.asStateFlow()

    init {
        loadExhibitions()
    }

    fun loadExhibitions() {
        viewModelScope.launch {
            _uiState.value = MyExhibitionsUiState.Loading
            // Obtiene el ID del artista autenticado desde la sesión
            val artistId = sessionManager.getUserId()
            if (artistId == -1L) {
                _uiState.value = MyExhibitionsUiState.Error("No hay sesión activa")
                return@launch
            }
            repository.getArtistProfile(artistId).fold(
                onSuccess = { profile ->
                    _uiState.value = MyExhibitionsUiState.Success(profile)
                },
                onFailure = { error ->
                    _uiState.value = MyExhibitionsUiState.Error(error.message ?: "Error al cargar las exposiciones")
                }
            )
        }
    }
}

sealed interface MyExhibitionsUiState {
    object Loading : MyExhibitionsUiState
    data class Success(val profile: ArtistProfile) : MyExhibitionsUiState
    data class Error(val message: String) : MyExhibitionsUiState
}
