package com.antigravity.swart.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.domain.model.GovBaliza
import com.antigravity.swart.domain.model.PropuestaBaliza
import com.antigravity.swart.domain.model.GeocodingResult
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddressSuggestion(val displayName: String, val lat: Double, val lon: Double)

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
    val endDate: Long? = null,
    // Empty balizas
    val emptyBalizas: List<com.antigravity.swart.domain.model.EmptyBaliza> = emptyList(),
    val selectedEmptyBaliza: com.antigravity.swart.domain.model.EmptyBaliza? = null,
    val selectedEmptyBalizaAddress: String? = null,
    val isLoadingBalizaAddress: Boolean = false,
    val isAddressSearchOpen: Boolean = false,
    val addressQuery: String = "",
    val addressSuggestions: List<AddressSuggestion> = emptyList(),
    val isSearchingAddress: Boolean = false,
    val addressSearchError: String? = null,
    val isCreatingEmptyBaliza: Boolean = false,
    // Propuestas
    val isProposalFormOpen: Boolean = false,
    val successMessage: String? = null,
    // Gov balizas
    val govBalizas: List<GovBaliza> = emptyList(),
    val isGovPanelOpen: Boolean = false,
    val selectedGovBalizaId: Long? = null,
    val currentUserId: Long = -1L
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(currentUserId = sessionManager.getUserId()))
    val uiState = _uiState.asStateFlow()
    
    private var pendingExhibitionId: Long? = null
    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        getMapPins()
        loadEmptyBalizas()
        loadGovBalizas()
    }

    fun getMapPins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getMapPins().fold(
                onSuccess = { pins ->
                    println("MAP_DEBUG: Loaded ${pins.size} pins")
                    var selected = _uiState.value.selectedPin
                    pendingExhibitionId?.let { pendingId ->
                        selected = pins.find { it.idExposicion == pendingId }
                        pendingExhibitionId = null
                    }
                    _uiState.value = _uiState.value.copy(
                        pins = pins,
                        filteredPins = pins,
                        isLoading = false,
                        selectedPin = selected
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

    fun selectExhibition(exhibitionId: Long) {
        if (exhibitionId == -1L) return
        val currentPins = _uiState.value.pins
        if (currentPins.isNotEmpty()) {
            val pin = currentPins.find { it.idExposicion == exhibitionId }
            if (pin != null) {
                _uiState.value = _uiState.value.copy(selectedPin = pin)
            }
        } else {
            pendingExhibitionId = exhibitionId
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
        _uiState.value = _uiState.value.copy(selectedPin = pin, selectedEmptyBaliza = null)
    }

    fun onDismissSelectedPin() {
        _uiState.value = _uiState.value.copy(selectedPin = null)
    }

    fun loadEmptyBalizas() {
        viewModelScope.launch {
            repository.getEmptyBalizas().fold(
                onSuccess = { list -> _uiState.value = _uiState.value.copy(emptyBalizas = list) },
                onFailure = { /* silently ignore */ }
            )
            repository.getBalizasGubernamentales().fold(
                onSuccess = { list -> _uiState.value = _uiState.value.copy(govBalizas = list) },
                onFailure = { /* silently ignore */ }
            )
        }
    }

    fun toggleAddressSearch(open: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAddressSearchOpen = open,
            addressQuery = "",
            addressSuggestions = emptyList(),
            addressSearchError = null,
            isSearchingAddress = false,
            selectedPin = null,
            selectedEmptyBaliza = null
        )
    }

    private fun loadGovBalizas() {
        viewModelScope.launch {
            repository.getBalizasGubernamentales().fold(
                onSuccess = { balizas ->
                    _uiState.value = _uiState.value.copy(govBalizas = balizas)
                },
                onFailure = { /* ignore or log */ }
            )
        }
    }

    fun onAddressQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(addressQuery = query, addressSearchError = null)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            if (query.isNotBlank()) {
                searchAddress(query)
            } else {
                _uiState.value = _uiState.value.copy(addressSuggestions = emptyList())
            }
        }
    }

    fun searchAddress(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingAddress = true, addressSearchError = null)
            repository.verifyAddress(query).fold(
                onSuccess = { results ->
                    val suggestions = results.map { result ->
                        AddressSuggestion(
                            displayName = result.displayName,
                            lat = result.lat,
                            lon = result.lon
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        addressSuggestions = suggestions,
                        isSearchingAddress = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSearchingAddress = false,
                        addressSearchError = "No se encontró la dirección."
                    )
                }
            )
        }
    }

    fun placeEmptyBaliza(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingEmptyBaliza = true)
            repository.createEmptyBaliza(lat, lon, sessionManager.getUserId()).fold(
                onSuccess = { baliza ->
                    _uiState.value = _uiState.value.copy(
                        emptyBalizas = _uiState.value.emptyBalizas + baliza,
                        isCreatingEmptyBaliza = false,
                        isAddressSearchOpen = false
                    )
                },
                onFailure = { throwable ->
                    android.util.Log.e("MapViewModel", "Error al crear baliza vacía", throwable)
                    _uiState.value = _uiState.value.copy(
                        isCreatingEmptyBaliza = false,
                        addressSearchError = "Error: ${throwable.message ?: "Fallo desconocido"}"
                    )
                }
            )
        }
    }

    fun onEmptyBalizaClick(baliza: com.antigravity.swart.domain.model.EmptyBaliza) {
        _uiState.value = _uiState.value.copy(
            selectedEmptyBaliza = baliza,
            selectedPin = null,
            selectedEmptyBalizaAddress = null,
            isLoadingBalizaAddress = true
        )
        viewModelScope.launch {
            repository.reverseGeocode(baliza.lat, baliza.lon).fold(
                onSuccess = { result ->
                    _uiState.value = _uiState.value.copy(
                        selectedEmptyBalizaAddress = result.displayName,
                        isLoadingBalizaAddress = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        selectedEmptyBalizaAddress = "${"%.5f".format(baliza.lat)}, ${"%.5f".format(baliza.lon)}",
                        isLoadingBalizaAddress = false
                    )
                }
            )
        }
    }

    fun dismissEmptyBaliza() {
        _uiState.value = _uiState.value.copy(
            selectedEmptyBaliza = null,
            selectedEmptyBalizaAddress = null,
            isLoadingBalizaAddress = false
        )
    }

    fun deleteEmptyBaliza(id: Long) {
        viewModelScope.launch {
            repository.deleteEmptyBaliza(id).fold(
                onSuccess = { success ->
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            emptyBalizas = _uiState.value.emptyBalizas.filter { it.id != id },
                            selectedEmptyBaliza = null
                        )
                    }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    // Propuestas
    fun toggleProposalForm(open: Boolean) {
        _uiState.value = _uiState.value.copy(isProposalFormOpen = open)
    }

    fun sendProposal(titulo: String, descrip: String, start: String, end: String, category: String, price: Double?) {
        val baliza = _uiState.value.selectedEmptyBaliza ?: return
        viewModelScope.launch {
            val req = com.antigravity.swart.data.remote.dto.PropuestaRequest(
                idArtista = sessionManager.getUserId(),
                titulo = titulo,
                descrip = descrip,
                fechaInicio = start,
                fechaFin = end,
                precio = price,
                categoria = category
            )
            repository.createPropuestaBaliza(baliza.id, req).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProposalFormOpen = false,
                        selectedEmptyBaliza = null,
                        successMessage = "Propuesta enviada con éxito"
                    )
                },
                onFailure = { /* show error */ }
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // Gov Balizas
    fun onGovBalizaClick(id: Long) {
        _uiState.value = _uiState.value.copy(
            isGovPanelOpen = true,
            selectedGovBalizaId = id,
            selectedPin = null,
            selectedEmptyBaliza = null
        )
    }

    fun dismissGovPanel() {
        _uiState.value = _uiState.value.copy(isGovPanelOpen = false, selectedGovBalizaId = null)
    }
}
