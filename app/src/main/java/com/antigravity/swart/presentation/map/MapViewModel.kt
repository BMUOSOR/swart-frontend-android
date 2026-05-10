package com.antigravity.swart.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val pins: List<MapPin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPin: MapPin? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getMapPins()
    }

    fun getMapPins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getMapPins().fold(
                onSuccess = { pins ->
                    println("MAP_DEBUG: Loaded ${pins.size} pins")
                    _uiState.value = _uiState.value.copy(pins = pins, isLoading = false)
                },
                onFailure = { error ->
                    println("MAP_DEBUG: Error loading pins: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
            )
        }
    }

    fun onPinClick(pin: MapPin) {
        _uiState.value = _uiState.value.copy(selectedPin = pin)
    }

    fun onDismissSelectedPin() {
        _uiState.value = _uiState.value.copy(selectedPin = null)
    }
}
