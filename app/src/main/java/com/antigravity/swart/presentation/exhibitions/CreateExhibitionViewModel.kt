package com.antigravity.swart.presentation.exhibitions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.data.remote.dto.CreateExhibitionRequest
import com.antigravity.swart.data.remote.dto.MutualArtistDto
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
class CreateExhibitionViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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

    private val _categoria = MutableStateFlow("")
    val categoria: StateFlow<String> = _categoria.asStateFlow()

    private val _isColaborativa = MutableStateFlow(false)
    val isColaborativa: StateFlow<Boolean> = _isColaborativa.asStateFlow()

    private val _artistasMutuos = MutableStateFlow<List<MutualArtistDto>>(emptyList())
    val artistasMutuos: StateFlow<List<MutualArtistDto>> = _artistasMutuos.asStateFlow()

    private val _artistasInvitados = MutableStateFlow<List<MutualArtistDto>>(emptyList())
    val artistasInvitados: StateFlow<List<MutualArtistDto>> = _artistasInvitados.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _verificationSuccess = MutableStateFlow<String?>(null)
    val verificationSuccess: StateFlow<String?> = _verificationSuccess.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _events = MutableSharedFlow<CreateExhibitionEvent>()
    val events: SharedFlow<CreateExhibitionEvent> = _events.asSharedFlow()

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
    fun onCategoriaSelected(cat: String) { _categoria.value = cat }

    fun onColaborativaToggle(value: Boolean) {
        _isColaborativa.value = value
        if (value && _artistasMutuos.value.isEmpty()) {
            loadMutuals()
        }
    }

    fun toggleInvitado(artista: MutualArtistDto) {
        val current = _artistasInvitados.value.toMutableList()
        if (current.any { it.id == artista.id }) {
            current.removeAll { it.id == artista.id }
        } else {
            current.add(artista)
        }
        _artistasInvitados.value = current
    }

    fun loadMutuals() {
        viewModelScope.launch {
            val artistId = sessionManager.getUserId()
            repository.getMutuals(artistId).fold(
                onSuccess = { _artistasMutuos.value = it },
                onFailure = { /* ignorar */ }
            )
        }
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
                onSuccess = { result ->
                    _ubicacion.value = result.displayName
                    if (!fromAddressField && _nombreLugar.value.isBlank()) {
                        _nombreLugar.value = result.displayName
                    }
                    _verificationSuccess.value = "Dirección encontrada y verificada"
                    _isVerifying.value = false
                },
                onFailure = {
                    _verificationError.value = if (fromAddressField)
                        "La dirección no es válida o no existe"
                    else
                        "Lugar no encontrado. Introduce la dirección en el campo de abajo."
                    _isVerifying.value = false
                }
            )
        }
    }

    fun createExhibition() {
        val artistaId = sessionManager.getUserId()
        if (_titulo.value.isBlank()) {
            viewModelScope.launch { _events.emit(CreateExhibitionEvent.Error("El título es obligatorio")) }
            return
        }
        viewModelScope.launch {
            _isCreating.value = true
            val request = CreateExhibitionRequest(
                titulo = _titulo.value,
                descrip = _descripcion.value.ifBlank { null },
                nombreLugar = _nombreLugar.value.ifBlank { null },
                ubicacion = _ubicacion.value.ifBlank { null },
                fechaInicio = _fechaInicio.value.ifBlank { null },
                fechaFin = _fechaFin.value.ifBlank { null },
                imgUrl = null,
                precio = null,
                activa = true,
                esColaborativa = _isColaborativa.value,
                artistaId = artistaId,
                artistasInvitadosIds = _artistasInvitados.value.map { it.id }
            )
            repository.createExhibition(request).fold(
                onSuccess = { newId ->
                    _isCreating.value = false
                    _events.emit(CreateExhibitionEvent.CreatedSuccessfully(newId))
                },
                onFailure = { error ->
                    _isCreating.value = false
                    _events.emit(CreateExhibitionEvent.Error(error.message ?: "Error al crear"))
                }
            )
        }
    }
}

sealed interface CreateExhibitionEvent {
    data class CreatedSuccessfully(val newId: Long) : CreateExhibitionEvent
    data class Error(val message: String) : CreateExhibitionEvent
}
