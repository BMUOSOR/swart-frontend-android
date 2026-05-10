package com.antigravity.swart.data.remote.dto

data class LoginRequest(
    val usuario: String,
    val password: String,
    val role: String
)

data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val usuario: String,
    val password: String,
    val role: String,
    val confirmAddRole: Boolean = false
)

data class AuthResponse(
    val id: Long,
    val nombre: String,
    val apellidos: String?,
    val usuario: String,
    val role: String,
    val token: String? = null
)

data class ErrorResponse(
    val error: String,
    val nombre: String? = null,
    val apellidos: String? = null
)
