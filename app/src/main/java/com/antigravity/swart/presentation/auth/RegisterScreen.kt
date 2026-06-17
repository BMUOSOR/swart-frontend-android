package com.antigravity.swart.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.res.painterResource
import com.antigravity.swart.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.swart.presentation.theme.*

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val usuario by viewModel.usuario.collectAsState()
    val password by viewModel.password.collectAsState()
    val nombre by viewModel.nombre.collectAsState()
    val apellidos by viewModel.apellidos.collectAsState()
    val role by viewModel.role.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val showUserExistsSameRoleDialog by viewModel.showUserExistsSameRoleDialog.collectAsState()
    val showUserExistsDifferentRoleDialog by viewModel.showUserExistsDifferentRoleDialog.collectAsState()
    val isAddingRole by viewModel.isAddingRole.collectAsState()

    if (showUserExistsSameRoleDialog) {
        val otherRole = if (role == "artista") "interesado" else "artista"
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserExistsSameRoleDialog() },
            title = { Text("Cuenta existente", color = TextWhite) },
            text = { Text("Ya tienes una cuenta de $role. ¿Quieres crear una cuenta de $otherRole?", color = TextWhite) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissUserExistsSameRoleDialog()
                    viewModel.onRoleChange(otherRole)
                    viewModel.setAddingRole(true)
                }) {
                    Text("Sí, cambiar rol", color = ArtistaGradientStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUserExistsSameRoleDialog() }) {
                    Text("Cancelar", color = TextGray)
                }
            },
            containerColor = CardBackground
        )
    }

    if (showUserExistsDifferentRoleDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserExistsDifferentRoleDialog() },
            title = { Text("Usuario existente", color = TextWhite) },
            text = { Text("Ya tienes una cuenta registrada. ¿Quieres añadir el perfil de $role a tu cuenta actual?", color = TextWhite) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissUserExistsDifferentRoleDialog()
                    viewModel.setAddingRole(true)
                    viewModel.registerConfirmed()
                }) {
                    Text("Sí, añadir perfil", color = ArtistaGradientStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUserExistsDifferentRoleDialog() }) {
                    Text("Cancelar", color = TextGray)
                }
            },
            containerColor = CardBackground
        )
    }

    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.dise_o_de_la_ui_logotipo__bien_),
            contentDescription = "Logo",
            modifier = Modifier.size(164.dp)
        )


        Text(
            text = "Crear Cuenta",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Únete a SWART y descubre el mejor arte.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        RoleSwitch(
            activeRole = role,
            onRoleChange = viewModel::onRoleChange,
            enabled = !isAddingRole
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthField(
            value = nombre,
            onValueChange = viewModel::onNombreChange,
            placeholder = "Nombre",
            icon = Icons.Default.Person,
            activeRole = role,
            enabled = !isAddingRole
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthField(
            value = apellidos,
            onValueChange = viewModel::onApellidosChange,
            placeholder = "Apellidos",
            icon = Icons.Default.Person,
            activeRole = role,
            enabled = !isAddingRole
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthField(
            value = usuario,
            onValueChange = viewModel::onUsuarioChange,
            placeholder = "Usuario",
            icon = Icons.Default.AccountCircle,
            activeRole = role,
            enabled = !isAddingRole
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            activeRole = role,
            enabled = !isAddingRole
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock, // Reusing lock
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = error!!, color = Color(0xFFE57373), fontSize = 14.sp)
            }
        }

        PrimaryButton(
            text = "Registrarse",
            activeRole = role,
            onClick = viewModel::register,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "¿Ya tienes cuenta? ", color = TextGray, fontSize = 14.sp)
            Text(
                text = "Inicia sesión",
                color = if (role == "artista") ArtistaGradientStart else InteresadoGradientStart,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { 
                    viewModel.setAddingRole(false)
                    onNavigateToLogin() 
                }
            )
        }
    }
}
