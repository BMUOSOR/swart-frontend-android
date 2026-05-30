package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.domain.model.ArtworkDetail
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
class EditArtworkViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artworkId: Long = savedStateHandle.get<Long>("artworkId") ?: -1L

    private val _uiState = MutableStateFlow<EditArtworkUiState>(EditArtworkUiState.Loading)
    val uiState: StateFlow<EditArtworkUiState> = _uiState.asStateFlow()

    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _precioText = MutableStateFlow("")
    val precioText: StateFlow<String> = _precioText.asStateFlow()

    private val _disponibleCompra = MutableStateFlow(false)
    val disponibleCompra: StateFlow<Boolean> = _disponibleCompra.asStateFlow()

    private val _categoriasExposicion = MutableStateFlow<List<String>>(emptyList())
    val categoriasExposicion: StateFlow<List<String>> = _categoriasExposicion.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    private val _tagsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val tagsSeleccionados: StateFlow<Set<String>> = _tagsSeleccionados.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _events = MutableSharedFlow<EditArtworkEvent>()
    val events: SharedFlow<EditArtworkEvent> = _events.asSharedFlow()

    init {
        loadArtworkDetail()
    }

    private fun loadArtworkDetail() {
        if (artworkId == -1L) {
            _uiState.value = EditArtworkUiState.Error("ID de obra inválido")
            return
        }
        viewModelScope.launch {
            _uiState.value = EditArtworkUiState.Loading
            repository.getArtworkDetail(artworkId).fold(
                onSuccess = { detail ->
                    _titulo.value = detail.titulo
                    _descripcion.value = detail.descrip ?: ""
                    _precioText.value = (detail.precio ?: 0.0).toString()
                    _disponibleCompra.value = detail.disponibleCompra
                    _categoriasExposicion.value = detail.categoriasExposicion
                    _imageUrl.value = detail.imgUrl

                    val initialCat = detail.categoriasExposicion.firstOrNull() ?: "Pintura"
                    _categoriaSeleccionada.value = initialCat
                    _tagsSeleccionados.value = detail.tags.toSet()

                    _uiState.value = EditArtworkUiState.Success(detail)
                },
                onFailure = { error ->
                    _uiState.value = EditArtworkUiState.Error(error.message ?: "Error al cargar la obra")
                }
            )
        }
    }

    fun onTituloChange(value: String) { _titulo.value = value }
    fun onDescripcionChange(value: String) { _descripcion.value = value }
    fun onPrecioChange(value: String) { _precioText.value = value }
    fun onDisponibleCompraChange(value: Boolean) { _disponibleCompra.value = value }

    fun onCategoriaSelected(cat: String) {
        _categoriaSeleccionada.value = cat
    }

    fun onTagToggle(tag: String) {
        val current = _tagsSeleccionados.value.toMutableList()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(0, tag)
        }
        _tagsSeleccionados.value = current.toSet()
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.value = EditArtworkUiState.Saving
            val price = _precioText.value.toDoubleOrNull() ?: 0.0
            repository.updateArtwork(
                id = artworkId,
                titulo = _titulo.value,
                descrip = _descripcion.value.ifBlank { null },
                precio = price,
                disponibleCompra = _disponibleCompra.value,
                tags = _tagsSeleccionados.value.toList()
            ).fold(
                onSuccess = {
                    _events.emit(EditArtworkEvent.SavedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.value = EditArtworkUiState.Error(error.message ?: "Error al guardar")
                    _events.emit(EditArtworkEvent.SaveError(error.message ?: "Error al guardar"))
                }
            )
        }
    }

    fun deleteArtwork() {
        viewModelScope.launch {
            _uiState.value = EditArtworkUiState.Saving
            repository.deleteArtwork(artworkId).fold(
                onSuccess = {
                    _events.emit(EditArtworkEvent.DeletedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.value = EditArtworkUiState.Error(error.message ?: "Error al eliminar la obra")
                }
            )
        }
    }
}

sealed interface EditArtworkUiState {
    object Loading : EditArtworkUiState
    object Saving : EditArtworkUiState
    data class Success(val detail: ArtworkDetail) : EditArtworkUiState
    data class Error(val message: String) : EditArtworkUiState
}

sealed interface EditArtworkEvent {
    object SavedSuccessfully : EditArtworkEvent
    object DeletedSuccessfully : EditArtworkEvent
    data class SaveError(val message: String) : EditArtworkEvent
}
