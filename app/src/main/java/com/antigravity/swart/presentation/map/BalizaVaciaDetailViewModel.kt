package com.antigravity.swart.presentation.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.data.remote.dto.BalizaVaciaDetailDto
import com.antigravity.swart.data.remote.dto.UpdateBalizaVaciaRequest
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.location.Geocoder
import java.util.Locale
import okhttp3.MultipartBody
import org.json.JSONArray
import javax.inject.Inject

data class BalizaVaciaDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val baliza: BalizaVaciaDetailDto? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val isPropietario: Boolean = false,
    val direccion: String? = null,
    // Editable fields (only for propietario)
    val titulo: String = "",
    val descripcion: String = "",
    val categoriasList: List<String> = emptyList(),
    val dimensiones: String = "",
    val plantas: String = "",
    val fotosList: List<String> = emptyList(),
    val isProposalFormOpen: Boolean = false
)

@HiltViewModel
class BalizaVaciaDetailViewModel @Inject constructor(
    private val repository: ExhibitionRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val balizaId: Long = savedStateHandle.get<Long>("balizaId") ?: -1L

    private val _uiState = MutableStateFlow(BalizaVaciaDetailUiState())
    val uiState: StateFlow<BalizaVaciaDetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getBalizaVaciaDetail(balizaId).fold(
                onSuccess = { dto ->
                    val currentUserId = sessionManager.getUserId()
                    val isProp = dto.idPropietario == currentUserId
                    val cats = dto.categorias?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                    val parsedFotos = mutableListOf<String>()
                    if (!dto.fotos.isNullOrBlank()) {
                        try {
                            val arr = JSONArray(dto.fotos)
                            for (i in 0 until arr.length()) {
                                parsedFotos.add(arr.getString(i))
                            }
                        } catch (e: Exception) {
                            parsedFotos.add(dto.fotos)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        baliza = dto,
                        isPropietario = isProp,
                        titulo = dto.titulo ?: "",
                        descripcion = dto.descripcion ?: "",
                        categoriasList = cats,
                        dimensiones = dto.dimensiones ?: "",
                        plantas = dto.plantas ?: "",
                        fotosList = parsedFotos
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun onTituloChange(v: String) { _uiState.value = _uiState.value.copy(titulo = v) }
    fun onDescripcionChange(v: String) { _uiState.value = _uiState.value.copy(descripcion = v) }
    fun onDimensionesChange(v: String) { _uiState.value = _uiState.value.copy(dimensiones = v) }
    fun onPlantasChange(v: String) { _uiState.value = _uiState.value.copy(plantas = v) }

    fun toggleCategoria(cat: String) {
        val current = _uiState.value.categoriasList.toMutableList()
        if (current.contains(cat)) current.remove(cat) else current.add(cat)
        _uiState.value = _uiState.value.copy(categoriasList = current)
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val req = UpdateBalizaVaciaRequest(
                titulo = state.titulo.ifBlank { null },
                descripcion = state.descripcion.ifBlank { null },
                categorias = state.categoriasList.joinToString(",").ifBlank { null },
                dimensiones = state.dimensiones.ifBlank { null },
                plantas = state.plantas.ifBlank { null },
                fotos = JSONArray(state.fotosList).toString()
            )
            repository.updateBalizaVacia(balizaId, req).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, successMessage = "Cambios guardados")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }

    fun loadDireccion(context: Context, lat: Double, lon: Double) {
        if (_uiState.value.direccion != null) return
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(lat, lon, 1)
                }
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val addressLine = address.getAddressLine(0) ?: "${address.thoroughfare ?: ""} ${address.subThoroughfare ?: ""}".trim()
                    _uiState.value = _uiState.value.copy(direccion = addressLine.ifBlank { "Ubicación genérica" })
                } else {
                    _uiState.value = _uiState.value.copy(direccion = "Dirección no encontrada")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(direccion = "$lat, $lon")
            }
        }
    }

    fun uploadSalaPhoto(filePart: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.uploadImage(filePart).fold(
                onSuccess = { url ->
                    val newList = _uiState.value.fotosList.toMutableList()
                    newList.add(url)
                    _uiState.value = _uiState.value.copy(fotosList = newList)
                    savePhotosToBackend(newList)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Error al subir foto: ${e.message}")
                }
            )
        }
    }

    fun removeSalaPhoto(url: String) {
        val newList = _uiState.value.fotosList.toMutableList()
        newList.remove(url)
        _uiState.value = _uiState.value.copy(fotosList = newList)
        savePhotosToBackend(newList)
    }

    private fun savePhotosToBackend(fotosList: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val jsonFotos = JSONArray(fotosList).toString()
            val req = UpdateBalizaVaciaRequest(fotos = jsonFotos)
            repository.updateBalizaVacia(balizaId, req).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, isLoading = false, successMessage = "Foto actualizada")
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, isLoading = false, error = e.message)
                }
            )
        }
    }

    fun toggleProposalForm(open: Boolean) {
        _uiState.value = _uiState.value.copy(isProposalFormOpen = open)
    }

    fun sendProposal(titulo: String, descrip: String, start: String, end: String, category: String, price: Double?) {
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
            repository.createPropuestaBaliza(balizaId, req).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isProposalFormOpen = false,
                        successMessage = "Propuesta enviada con éxito"
                    )
                },
                onFailure = { e -> 
                    _uiState.value = _uiState.value.copy(error = "Error al enviar propuesta: ${e.message}")
                }
            )
        }
    }

    fun getCurrentUserAvatarUrl(): String = sessionManager.getImgUrl()
    fun getCurrentUserId(): Long = sessionManager.getUserId()
}
