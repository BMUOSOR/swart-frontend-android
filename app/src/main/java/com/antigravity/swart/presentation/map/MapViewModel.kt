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
    val filteredPins: List<MapPin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPin: MapPin? = null,
    val searchQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isFilterSheetVisible: Boolean = false,
    val maxPrice: Float = 100f,
    val startDate: Long? = null,
    val endDate: Long? = null
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
                    _uiState.value = _uiState.value.copy(
                        pins = pins,
                        filteredPins = pins,
                        isLoading = false
                    )
                    applyFilters() // Apply filters to initial data
                },
                onFailure = { error ->
                    println("MAP_DEBUG: Error loading pins: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onToggleTag(tag: String) {
        val currentTags = _uiState.value.selectedTags
        val newTags = if (currentTags.contains(tag)) {
            currentTags - tag
        } else {
            currentTags + tag
        }
        _uiState.value = _uiState.value.copy(selectedTags = newTags)
        applyFilters()
    }

    fun toggleFilterSheet(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = visible)
    }

    fun onPriceChange(price: Float) {
        _uiState.value = _uiState.value.copy(maxPrice = price)
        applyFilters()
    }

    fun onDateRangeChange(start: Long?, end: Long?) {
        _uiState.value = _uiState.value.copy(startDate = start, endDate = end)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val query = state.searchQuery.lowercase()
        val tags = state.selectedTags.map { it.lowercase() }

        val filtered = state.pins.filter { pin ->
            val matchesQuery = query.isEmpty() || 
                pin.titulo.lowercase().contains(query) || 
                pin.galeria.lowercase().contains(query)
            
            val matchesTags = tags.isEmpty() || tags.contains(pin.mainTag.lowercase())
            
            val matchesDate = if (state.startDate != null && state.endDate != null && pin.startDate != null && pin.endDate != null) {
                // Overlap condition: FilterStart <= ExhibEnd && FilterEnd >= ExhibStart
                state.startDate <= pin.endDate && state.endDate >= pin.startDate
            } else if (state.startDate != null && pin.startDate != null) {
                // If only start date is filtered, show if exhibition is still active or starts after
                pin.endDate ?: pin.startDate >= state.startDate
            } else {
                true
            }
            
            matchesQuery && matchesTags && matchesDate
        }
        
        _uiState.value = _uiState.value.copy(filteredPins = filtered)
    }

    fun onPinClick(pin: MapPin) {
        _uiState.value = _uiState.value.copy(selectedPin = pin)
    }

    fun onDismissSelectedPin() {
        _uiState.value = _uiState.value.copy(selectedPin = null)
    }
}
