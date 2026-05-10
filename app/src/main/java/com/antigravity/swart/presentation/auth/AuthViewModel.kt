package com.antigravity.swart.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.swart.data.remote.dto.AuthResponse
import com.antigravity.swart.data.repository.RoleMissingException
import com.antigravity.swart.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow("")
    val usuario = _usuario.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre = _nombre.asStateFlow()

    private val _apellidos = MutableStateFlow("")
    val apellidos = _apellidos.asStateFlow()

    private val _role = MutableStateFlow("interesado")
    val role = _role.asStateFlow()

    private val _isAddingRole = MutableStateFlow(false)
    val isAddingRole = _isAddingRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _authSuccess = MutableStateFlow<AuthResponse?>(null)
    val authSuccess = _authSuccess.asStateFlow()

    private val _showRoleMissingDialog = MutableStateFlow(false)
    val showRoleMissingDialog = _showRoleMissingDialog.asStateFlow()

    private val _showUserExistsSameRoleDialog = MutableStateFlow(false)
    val showUserExistsSameRoleDialog = _showUserExistsSameRoleDialog.asStateFlow()

    private val _showUserExistsDifferentRoleDialog = MutableStateFlow(false)
    val showUserExistsDifferentRoleDialog = _showUserExistsDifferentRoleDialog.asStateFlow()

    fun onUsuarioChange(newUsuario: String) { 
        _usuario.value = newUsuario 
        _error.value = null
    }
    fun onPasswordChange(newPassword: String) { 
        _password.value = newPassword 
        _error.value = null
    }
    fun onNombreChange(newNombre: String) { 
        _nombre.value = newNombre 
        _error.value = null
    }
    fun onApellidosChange(newApellidos: String) { 
        _apellidos.value = newApellidos 
        _error.value = null
    }
    fun onRoleChange(newRole: String) { _role.value = newRole }

    fun dismissRoleMissingDialog() { _showRoleMissingDialog.value = false }
    fun dismissUserExistsSameRoleDialog() { _showUserExistsSameRoleDialog.value = false }
    fun dismissUserExistsDifferentRoleDialog() { _showUserExistsDifferentRoleDialog.value = false }
    fun setAddingRole(adding: Boolean) { _isAddingRole.value = adding }

    fun login() {
        if (usuario.value.isBlank() || password.value.isBlank()) {
            _error.value = "Por favor, rellena todos los campos"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.login(usuario.value, password.value, role.value).fold(
                onSuccess = { response ->
                    _authSuccess.value = response
                },
                onFailure = { ex ->
                    if (ex is RoleMissingException) {
                        _nombre.value = ex.nombre ?: ""
                        _apellidos.value = ex.apellidos ?: ""
                        _showRoleMissingDialog.value = true
                    } else {
                        _error.value = ex.message
                    }
                }
            )
            
            _isLoading.value = false
        }
    }

    fun register() {
        if (usuario.value.isBlank() || password.value.isBlank() || nombre.value.isBlank() || apellidos.value.isBlank()) {
            _error.value = "Por favor, rellena todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.register(nombre.value, apellidos.value, usuario.value, password.value, role.value).fold(
                onSuccess = { response ->
                    _authSuccess.value = response
                },
                onFailure = { ex ->
                    if (ex is RoleMissingException && ex.message == "USER_EXISTS_SAME_ROLE") {
                        _nombre.value = ex.nombre ?: ""
                        _apellidos.value = ex.apellidos ?: ""
                        _showUserExistsSameRoleDialog.value = true
                    } else if (ex is RoleMissingException && ex.message == "USER_EXISTS_DIFFERENT_ROLE") {
                        _nombre.value = ex.nombre ?: ""
                        _apellidos.value = ex.apellidos ?: ""
                        _showUserExistsDifferentRoleDialog.value = true
                    } else {
                        _error.value = ex.message
                    }
                }
            )

            _isLoading.value = false
        }
    }

    fun registerConfirmed() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.register(nombre.value, apellidos.value, usuario.value, password.value, role.value, confirmAddRole = true).fold(
                onSuccess = { response ->
                    _authSuccess.value = response
                },
                onFailure = { ex ->
                    _error.value = ex.message
                }
            )
            _isLoading.value = false
        }
    }
}
