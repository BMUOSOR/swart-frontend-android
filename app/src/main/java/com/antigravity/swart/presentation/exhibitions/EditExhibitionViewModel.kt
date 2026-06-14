package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.domain.model.ArtistBasic
import com.antigravity.swart.domain.model.Artwork
import com.antigravity.swart.domain.model.ExhibitionDetail
import com.antigravity.swart.domain.repository.ExhibitionRepository
import com.antigravity.swart.core.SessionManager
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
    private val sessionManager: SessionManager,
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

    private val _ubicacion = MutableStateFlow("")
    val ubicacion: StateFlow<String> = _ubicacion.asStateFlow()

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

    private val _esColaborativa = MutableStateFlow(false)
    val esColaborativa: StateFlow<Boolean> = _esColaborativa.asStateFlow()

    private val _artistas = MutableStateFlow<List<ArtistBasic>>(emptyList())
    val artistas: StateFlow<List<ArtistBasic>> = _artistas.asStateFlow()

    val currentArtistId: Long = sessionManager.getUserId()

    // Geocoding validation states
    private val _verificationSuccess = MutableStateFlow<String?>(null)
    val verificationSuccess: StateFlow<String?> = _verificationSuccess.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

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
                    _ubicacion.value = detail.ubicacion ?: ""
                    _fechaInicio.value = detail.fechaInicio ?: ""
                    _fechaFin.value = detail.fechaFin ?: ""
                    _obras.value = detail.obras
                    _bannerUrl.value = detail.imgUrl
                    _esColaborativa.value = detail.esColaborativa
                    _artistas.value = detail.artistas
                    // Cargar sólo los tags de categoría de la exposición
                    val categories = detail.tags.filter { it in setOf("Pintura", "Escultura", "Fotografía") }.toSet()
                    _tagsSeleccionados.value = categories
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
    fun onUbicacionChange(value: String) {
        _ubicacion.value = value
        _verificationSuccess.value = null
        _verificationError.value = null
    }
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

    fun verifyLocationAddress(fromAddressField: Boolean = false) {
        val query = if (fromAddressField) _ubicacion.value else _nombreLugar.value
        if (query.isBlank()) {
            _verificationError.value = "Introduce un término de búsqueda"
            _verificationSuccess.value = null
            return
        }
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationError.value = null
            _verificationSuccess.value = null
            repository.verifyAddress(query).fold(
                onSuccess = { results ->
                    val result = results.firstOrNull()
                    if (result != null) {
                        _ubicacion.value = result.displayName
                        if (fromAddressField && _nombreLugar.value.isBlank()) {
                            _nombreLugar.value = result.displayName
                        }
                        _verificationSuccess.value = "Dirección encontrada y verificada"
                    } else {
                        _verificationError.value = "Lugar no encontrado."
                    }
                    _isVerifying.value = false
                },
                onFailure = { error ->
                    _verificationError.value = if (fromAddressField) {
                        "La dirección no es válida o no existe"
                    } else {
                        "Lugar no encontrado en el mapa. Por favor, introduce la dirección en el campo de abajo para verificarla."
                    }
                    _isVerifying.value = false
                }
            )
        }
    }

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun uploadBanner(filePart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            _isUploading.value = true
            repository.uploadImage(filePart).fold(
                onSuccess = { url ->
                    _bannerUrl.value = url
                    _isUploading.value = false
                },
                onFailure = { error ->
                    _isUploading.value = false
                    _events.emit(EditExhibitionEvent.SaveError("Fallo al subir banner: ${error.message}"))
                }
            )
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.value = EditExhibitionUiState.Saving
            repository.updateExhibition(
                id = exhibitionId,
                titulo = _titulo.value,
                descrip = _descripcion.value.ifBlank { null },
                nombreLugar = _nombreLugar.value.ifBlank { null },
                ubicacion = _ubicacion.value.ifBlank { null },
                fechaInicio = _fechaInicio.value.ifBlank { null },
                fechaFin = _fechaFin.value.ifBlank { null },
                imgUrl = _bannerUrl.value,
                tags = _tagsSeleccionados.value.toList()
            ).fold(
                onSuccess = {
                    _events.emit(EditExhibitionEvent.SavedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.value = EditExhibitionUiState.Error(error.message ?: "Error al guardar")
                    _events.emit(EditExhibitionEvent.SaveError(error.message ?: "Error al guardar: ${error.message}"))
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
