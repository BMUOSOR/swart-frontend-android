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
import javax.inject.Inject

data class BalizaVaciaDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val baliza: BalizaVaciaDetailDto? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val isPropietario: Boolean = false,
    // Editable fields (only for propietario)
    val titulo: String = "",
    val descripcion: String = "",
    val categoriasList: List<String> = emptyList(),
    val dimensiones: String = "",
    val salas: String = ""
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        baliza = dto,
                        isPropietario = isProp,
                        titulo = dto.titulo ?: "",
                        descripcion = dto.descripcion ?: "",
                        categoriasList = cats,
                        dimensiones = dto.dimensiones ?: "",
                        salas = dto.salas ?: ""
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
                salas = state.salas.ifBlank { null }
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

    fun getCurrentUserAvatarUrl(): String = sessionManager.getImgUrl()
    fun getCurrentUserId(): Long = sessionManager.getUserId()
}
