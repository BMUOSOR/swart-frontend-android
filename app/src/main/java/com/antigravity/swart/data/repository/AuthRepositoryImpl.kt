package com.antigravity.swart.data.repository

import com.antigravity.swart.data.remote.AuthApi
import com.antigravity.swart.data.remote.dto.LoginRequest
import com.antigravity.swart.data.remote.dto.RegisterRequest
import com.antigravity.swart.data.remote.dto.AuthResponse
import com.antigravity.swart.data.remote.dto.ErrorResponse
import com.antigravity.swart.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(usuario: String, password: String, role: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(usuario, password, role))
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            val errorRes = try { Gson().fromJson(errorBody, ErrorResponse::class.java) } catch (e: Exception) { null }
            
            if (errorRes?.error == "ROLE_MISSING") {
                Result.failure(RoleMissingException(errorRes.nombre, errorRes.apellidos, "ROLE_MISSING"))
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(nombre: String, apellidos: String, usuario: String, password: String, role: String, confirmAddRole: Boolean): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(nombre, apellidos, usuario, password, role, confirmAddRole))
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            val errorRes = try { Gson().fromJson(errorBody, ErrorResponse::class.java) } catch (e: Exception) { null }

            if (errorRes?.error == "USER_EXISTS_SAME_ROLE") {
                Result.failure(RoleMissingException(errorRes.nombre, errorRes.apellidos, "USER_EXISTS_SAME_ROLE"))
            } else if (errorRes?.error == "USER_EXISTS_DIFFERENT_ROLE") {
                Result.failure(RoleMissingException(errorRes.nombre, errorRes.apellidos, "USER_EXISTS_DIFFERENT_ROLE"))
            } else {
                Result.failure(Exception("El usuario ya existe o hubo un error"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de conexión"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class RoleMissingException(val nombre: String?, val apellidos: String?, message: String) : Exception(message)
