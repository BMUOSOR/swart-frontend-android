package com.antigravity.swart.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.core.SessionManager
import com.antigravity.swart.domain.repository.ExhibitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class UserProfileUiState(
    val nombre: String = "",
    val apellidos: String = "",
    val imgUrl: String = "",
    val location: String = "Valencia, España",
    val role: String = "",
    val pendingInvitationsCount: Int = 0,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val uploadError: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = sessionManager.getUserId()
        val role = sessionManager.getRole()
        val nombre = sessionManager.getNombre()
        val apellidos = sessionManager.getApellidos()
        val imgUrl = sessionManager.getImgUrl().ifBlank {
            "https://bkrmqkpxidmemzxhefoc.supabase.co/storage/v1/object/public/Imagenes/usuario_chica_3.jpg"
        }

        _uiState.value = UserProfileUiState(
            nombre = nombre,
            apellidos = apellidos,
            imgUrl = imgUrl,
            role = role,
            isLoading = true
        )

        if (userId != -1L) {
            viewModelScope.launch {
                repository.getInvitations(userId).fold(
                    onSuccess = { invitations ->
                        _uiState.value = _uiState.value.copy(
                            pendingInvitationsCount = invitations.size,
                            isLoading = false
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun uploadAndUpdateAvatar(uri: Uri, context: Context) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return
        _uiState.value = _uiState.value.copy(isUploadingAvatar = true, uploadError = null)
        viewModelScope.launch {
            try {
                // Read file on IO thread to avoid blocking the main thread
                val (bytes, mimeType) = withContext(Dispatchers.IO) {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val data = context.contentResolver.openInputStream(uri)?.readBytes()
                        ?: throw Exception("No se pudo leer la imagen seleccionada")
                    Pair(data, mime)
                }

                val ext = if (mimeType.contains("png")) "png" else "jpg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", "avatar.$ext", requestBody)

                repository.uploadImage(part).fold(
                    onSuccess = { imageUrl ->
                        repository.updateAvatar(userId, imageUrl).fold(
                            onSuccess = {
                                sessionManager.saveImgUrl(imageUrl)
                                _uiState.value = _uiState.value.copy(
                                    imgUrl = imageUrl,
                                    isUploadingAvatar = false,
                                    uploadError = null
                                )
                            },
                            onFailure = { err ->
                                _uiState.value = _uiState.value.copy(
                                    isUploadingAvatar = false,
                                    uploadError = "Error al guardar la foto: ${err.message}"
                                )
                            }
                        )
                    },
                    onFailure = { err ->
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false,
                            uploadError = "Error al subir la foto: ${err.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingAvatar = false,
                    uploadError = "Error: ${e.message}"
                )
            }
        }
    }

    fun clearUploadError() {
        _uiState.value = _uiState.value.copy(uploadError = null)
    }
}
