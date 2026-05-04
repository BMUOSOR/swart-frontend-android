package com.antigravity.swart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _allExhibitions = MutableStateFlow<List<Exhibition>>(emptyList())
    
    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        _isLoading, _allExhibitions, searchQuery, _error
    ) { isLoading, allExhibitions, query, error ->
        val filtered = if (query.isBlank()) {
            allExhibitions
        } else {
            allExhibitions.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artistName.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        HomeUiState(isLoading, filtered, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        loadFeed()
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
}
