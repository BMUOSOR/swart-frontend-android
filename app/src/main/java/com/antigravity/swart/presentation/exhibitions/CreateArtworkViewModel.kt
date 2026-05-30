package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.data.remote.dto.CreateArtworkRequest
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
class CreateArtworkViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exhibitionId: Long = savedStateHandle.get<Long>("exhibitionId") ?: -1L
    private val initialCategoria: String = savedStateHandle.get<String>("categoria") ?: "Pintura"

    private val _titulo = MutableStateFlow("")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _precioText = MutableStateFlow("")
    val precioText: StateFlow<String> = _precioText.asStateFlow()

    private val _disponibleCompra = MutableStateFlow(false)
    val disponibleCompra: StateFlow<Boolean> = _disponibleCompra.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow(initialCategoria)
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    private val _tagsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val tagsSeleccionados: StateFlow<Set<String>> = _tagsSeleccionados.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _events = MutableSharedFlow<CreateArtworkEvent>()
    val events: SharedFlow<CreateArtworkEvent> = _events.asSharedFlow()

    fun onTituloChange(v: String) { _titulo.value = v }
    fun onDescripcionChange(v: String) { _descripcion.value = v }
    fun onPrecioChange(v: String) { _precioText.value = v }
    fun onDisponibleCompraChange(v: Boolean) { _disponibleCompra.value = v }
    fun onCategoriaSelected(cat: String) { _categoriaSeleccionada.value = cat }
    fun onTagToggle(tag: String) {
        val curr = _tagsSeleccionados.value.toMutableList()
        if (curr.contains(tag)) {
            curr.remove(tag)
        } else {
            curr.add(0, tag)
        }
        _tagsSeleccionados.value = curr.toSet()
    }

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    fun uploadImage(filePart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            _isUploading.value = true
            repository.uploadImage(filePart).fold(
                onSuccess = { url ->
                    _imageUrl.value = url
                    _isUploading.value = false
                },
                onFailure = { error ->
                    _isUploading.value = false
                    _events.emit(CreateArtworkEvent.Error("Fallo al subir imagen: ${error.message}"))
                }
            )
        }
    }

    fun createArtwork() {
        if (_titulo.value.isBlank()) {
            viewModelScope.launch { _events.emit(CreateArtworkEvent.Error("El título es obligatorio")) }
            return
        }
        viewModelScope.launch {
            _isCreating.value = true
            val precio = _precioText.value.toDoubleOrNull() ?: 0.0
            val allTags = buildList {
                add(_categoriaSeleccionada.value)
                addAll(_tagsSeleccionados.value)
            }
            val artistId = sessionManager.getUserId()
            val request = CreateArtworkRequest(
                idExposicion = exhibitionId,
                idArtista = artistId,
                titulo = _titulo.value,
                descrip = _descripcion.value.ifBlank { null },
                imgUrl = _imageUrl.value,
                precio = precio,
                disponibleCompra = _disponibleCompra.value,
                tags = allTags
            )
            repository.createArtwork(request).fold(
                onSuccess = {
                    _isCreating.value = false
                    _events.emit(CreateArtworkEvent.CreatedSuccessfully)
                },
                onFailure = { error ->
                    _isCreating.value = false
                    _events.emit(CreateArtworkEvent.Error(error.message ?: "Error al crear la obra"))
                }
            )
        }
    }
}

sealed interface CreateArtworkEvent {
    object CreatedSuccessfully : CreateArtworkEvent
    data class Error(val message: String) : CreateArtworkEvent
}
