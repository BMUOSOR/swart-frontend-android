package com.antigravity.swart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.model.Exhibition
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val exhibitions: List<Exhibition> = emptyList(), // Lista filtrada
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _allExhibitions = MutableStateFlow<List<Exhibition>>(emptyList())
    
    val searchQuery = MutableStateFlow("")
    val userAvatar = MutableStateFlow("")
    
    val selectedTag = MutableStateFlow("Todos")
    val filterStartDate = MutableStateFlow("")
    val filterEndDate = MutableStateFlow("")
    val filterArtistName = MutableStateFlow("")
    val filterArtworkTag = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        _isLoading, _allExhibitions, searchQuery, selectedTag, filterStartDate, filterEndDate, filterArtistName, filterArtworkTag, _error
    ) { flowsArray ->
        val isLoading = flowsArray[0] as Boolean
        @Suppress("UNCHECKED_CAST")
        val allExhibitions = flowsArray[1] as List<Exhibition>
        val query = flowsArray[2] as String
        val tag = flowsArray[3] as String
        val startD = flowsArray[4] as String
        val endD = flowsArray[5] as String
        val artistN = flowsArray[6] as String
        val artworkT = flowsArray[7] as String
        val error = flowsArray[8] as String?

        var filtered = allExhibitions
        
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artistName.contains(query, ignoreCase = true)
            }
        }
        
        if (tag != "Todos") {
            filtered = filtered.filter { exhibition ->
                exhibition.tags.any { it.equals(tag, ignoreCase = true) }
            }
        }
        
        if (artistN.isNotBlank()) {
            filtered = filtered.filter {
                it.artistName.contains(artistN, ignoreCase = true)
            }
        }
        
        if (startD.isNotBlank()) {
            filtered = filtered.filter {
                val converted = convertDateInput(startD)
                if (converted != null) {
                    it.fechaInicio?.contains(converted) == true
                } else {
                    it.fechaInicio?.contains(startD) == true
                }
            }
        }
        
        if (endD.isNotBlank()) {
            filtered = filtered.filter {
                val converted = convertDateInput(endD)
                if (converted != null) {
                    it.fechaFin?.contains(converted) == true
                } else {
                    it.fechaFin?.contains(endD) == true
                }
            }
        }
        
        if (artworkT.isNotBlank()) {
            filtered = filtered.filter { exhibition ->
                exhibition.tags.any { it.contains(artworkT, ignoreCase = true) }
            }
        }
        
        HomeUiState(isLoading, filtered, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        loadFeed()
        userAvatar.value = sessionManager.getImgUrl().ifBlank {
            "https://bkrmqkpxidmemzxhefoc.supabase.co/storage/v1/object/public/Imagenes/usuario_chica_3.jpg"
        }
    }

    private fun loadFeed() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getFeed().fold(
                onSuccess = { exhibitions ->
                    _allExhibitions.value = exhibitions
                    _isLoading.value = false
                    _error.value = null
                },
                onFailure = { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Error desconocido"
                }
            )
        }
    }
    
    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun applyFilters(startDate: String, endDate: String, artistName: String, tag: String, artworkTag: String) {
        filterStartDate.value = startDate
        filterEndDate.value = endDate
        filterArtistName.value = artistName
        selectedTag.value = tag
        filterArtworkTag.value = artworkTag
    }

    private fun convertDateInput(input: String): String? {
        val parts = input.split("/")
        if (parts.size == 2) {
            val day = parts[0].trim().padStart(2, '0')
            val month = parts[1].trim().padStart(2, '0')
            return "$month-$day"
        }
        return null
    }
}
