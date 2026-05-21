package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.domain.model.Artwork
import com.antigravity.swart.domain.model.ExhibitionDetail
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditExhibitionViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exhibitionId: Long = savedStateHandle.get<Long>("exhibitionId") ?: -1L

    // ── UI State ──────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<EditExhibitionUiState>(EditExhibitionUiState.Loading)
    val uiState: StateFlow<EditExhibitionUiState> = _uiState.asStateFlow()

    // ── Form fields ───────────────────────────────────────────────────────
    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _nombreLugar = MutableStateFlow("")
    val nombreLugar: StateFlow<String> = _nombreLugar.asStateFlow()

    private val _fechaInicio = MutableStateFlow("")
    val fechaInicio: StateFlow<String> = _fechaInicio.asStateFlow()

    private val _fechaFin = MutableStateFlow("")
    val fechaFin: StateFlow<String> = _fechaFin.asStateFlow()

    private val _obras = MutableStateFlow<List<Artwork>>(emptyList())
    val obras: StateFlow<List<Artwork>> = _obras.asStateFlow()

    // Tags: Categoria primaria seleccionada (Pintura/Escultura/Fotografía)
    private val _categoriaSeleccionada = MutableStateFlow("")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    // Tags secundarios seleccionados
    private val _tagsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val tagsSeleccionados: StateFlow<Set<String>> = _tagsSeleccionados.asStateFlow()

    // Banner image URL
    private val _bannerUrl = MutableStateFlow<String?>(null)
    val bannerUrl: StateFlow<String?> = _bannerUrl.asStateFlow()

    // ── Events (one-shot) ─────────────────────────────────────────────────
    private val _events = MutableSharedFlow<EditExhibitionEvent>()
    val events: SharedFlow<EditExhibitionEvent> = _events.asSharedFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (exhibitionId == -1L) {
            _uiState.value = EditExhibitionUiState.Error("ID de exposición inválido")
            return
        }
        viewModelScope.launch {
            _uiState.value = EditExhibitionUiState.Loading
            repository.getExhibitionDetail(exhibitionId).fold(
                onSuccess = { detail ->
                    // Preload form fields
                    _titulo.value = detail.titulo
                    _descripcion.value = detail.descrip ?: ""
                    _nombreLugar.value = detail.nombreLugar ?: ""
                    _fechaInicio.value = detail.fechaInicio ?: ""
                    _fechaFin.value = detail.fechaFin ?: ""
                    _obras.value = detail.obras
                    _bannerUrl.value = detail.imgUrl
                    // Detect category from existing tags
                    val allTags = detail.tags.toSet()
                    val cat = when {
                        allTags.any { it.lowercase().contains("pintura") || it.lowercase().contains("óleo") || it.lowercase().contains("acuarela") } -> "Pintura"
                        allTags.any { it.lowercase().contains("escultura") || it.lowercase().contains("talla") || it.lowercase().contains("busto") } -> "Escultura"
                        allTags.any { it.lowercase().contains("fotografía") || it.lowercase().contains("foto") || it.lowercase().contains("analógica") } -> "Fotografía"
                        else -> ""
                    }
                    _categoriaSeleccionada.value = cat
                    _tagsSeleccionados.value = allTags
                    _uiState.value = EditExhibitionUiState.Success(detail)
                },
                onFailure = { error ->
                    _uiState.value = EditExhibitionUiState.Error(error.message ?: "Error al cargar la exposición")
                }
            )
        }
    }

    fun onTituloChange(value: String) { _titulo.value = value }
    fun onDescripcionChange(value: String) { _descripcion.value = value }
    fun onNombreLugarChange(value: String) { _nombreLugar.value = value }
    fun onFechaInicioChange(value: String) { _fechaInicio.value = value }
    fun onFechaFinChange(value: String) { _fechaFin.value = value }

    fun onCategoriaSelected(categoria: String) {
        _categoriaSeleccionada.value = categoria
        // Reset subtags when category changes
        _tagsSeleccionados.value = emptySet()
    }

    fun onTagToggle(tag: String) {
        val current = _tagsSeleccionados.value.toMutableSet()
        if (current.contains(tag)) current.remove(tag) else current.add(tag)
        _tagsSeleccionados.value = current
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.value = EditExhibitionUiState.Saving
            repository.updateExhibition(
                id = exhibitionId,
                titulo = _titulo.value,
                descrip = _descripcion.value.ifBlank { null },
                nombreLugar = _nombreLugar.value.ifBlank { null },
                ubicacion = null,
                fechaInicio = _fechaInicio.value.ifBlank { null },
                fechaFin = _fechaFin.value.ifBlank { null },
                tags = _tagsSeleccionados.value.toList()
            ).fold(
                onSuccess = {
                    _events.emit(EditExhibitionEvent.SavedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.value = EditExhibitionUiState.Error(error.message ?: "Error al guardar")
                    _events.emit(EditExhibitionEvent.SaveError(error.message ?: "Error al guardar"))
                }
            )
        }
    }

    fun deleteExhibition() {
        viewModelScope.launch {
            _uiState.value = EditExhibitionUiState.Saving
            repository.deleteExhibition(exhibitionId).fold(
                onSuccess = {
                    _events.emit(EditExhibitionEvent.DeletedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.value = EditExhibitionUiState.Error(error.message ?: "Error al eliminar")
                }
            )
        }
    }
}

sealed interface EditExhibitionUiState {
    object Loading : EditExhibitionUiState
    object Saving : EditExhibitionUiState
    data class Success(val detail: ExhibitionDetail) : EditExhibitionUiState
    data class Error(val message: String) : EditExhibitionUiState
}

sealed interface EditExhibitionEvent {
    object SavedSuccessfully : EditExhibitionEvent
    object DeletedSuccessfully : EditExhibitionEvent
    data class SaveError(val message: String) : EditExhibitionEvent
}
