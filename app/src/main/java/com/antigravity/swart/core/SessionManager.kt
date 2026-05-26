package com.antigravity.swart.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona la sesión del usuario autenticado.
 * Persiste el userId y role en SharedPreferences para que estén disponibles
 * en toda la aplicación entre pantallas.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("swart_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "user_role"
        private const val KEY_NOMBRE = "user_nombre"
        private const val KEY_APELLIDOS = "user_apellidos"
        private const val KEY_USUARIO = "user_usuario"
        private const val KEY_IMG_URL = "user_img_url"
    }

    fun saveSession(userId: Long, role: String, nombre: String, apellidos: String?, usuario: String, imgUrl: String?) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_APELLIDOS, apellidos ?: "")
            .putString(KEY_USUARIO, usuario)
            .putString(KEY_IMG_URL, imgUrl ?: "")
            .apply()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun getNombre(): String = prefs.getString(KEY_NOMBRE, "") ?: ""

    fun getApellidos(): String = prefs.getString(KEY_APELLIDOS, "") ?: ""

    fun getUsuario(): String = prefs.getString(KEY_USUARIO, "") ?: ""

    fun getImgUrl(): String = prefs.getString(KEY_IMG_URL, "") ?: ""

    fun isLoggedIn(): Boolean = getUserId() != -1L

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
