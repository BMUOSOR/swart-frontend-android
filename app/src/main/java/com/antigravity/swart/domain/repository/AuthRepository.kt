package com.antigravity.swart.domain.repository

import com.antigravity.swart.data.remote.dto.AuthResponse

interface AuthRepository {
    suspend fun login(usuario: String, password: String, role: String): Result<AuthResponse>
    suspend fun register(nombre: String, apellidos: String, usuario: String, password: String, role: String, confirmAddRole: Boolean = false): Result<AuthResponse>
}
