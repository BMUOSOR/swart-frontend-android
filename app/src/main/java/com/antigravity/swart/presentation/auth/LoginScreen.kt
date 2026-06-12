package com.antigravity.swart.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val usuario by viewModel.usuario.collectAsState()
    val password by viewModel.password.collectAsState()
    val role by viewModel.role.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val showRoleMissingDialog by viewModel.showRoleMissingDialog.collectAsState()

    if (showRoleMissingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRoleMissingDialog() },
            title = { Text("Cuenta no encontrada", color = TextWhite) },
            text = { Text("No tienes una cuenta de $role. ¿Deseas registrarte?", color = TextWhite) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissRoleMissingDialog()
                    viewModel.setAddingRole(true)
                    onNavigateToRegister()
                }) {
                    Text("Sí, registrarse", color = ArtistaGradientStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRoleMissingDialog() }) {
                    Text("Cancelar", color = TextGray)
                }
            },
            containerColor = CardBackground
        )
    }

    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .padding(top = 34.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.dise_o_de_la_ui_logotipo__1_),
            contentDescription = "Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(0.2.dp))

        Text(
            text = "Bienvenid@ a SWART",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia sesión para continuar descubriendo arte.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        RoleSwitch(
            activeRole = role,
            onRoleChange = viewModel::onRoleChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        AuthField(
            value = usuario,
            onValueChange = viewModel::onUsuarioChange,
            placeholder = "Usuario",
            icon = Icons.Default.Person,
            activeRole = role
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            activeRole = role
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock, // Reusing lock or similar
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = error!!, color = Color(0xFFE57373), fontSize = 14.sp)
            }
        }

        PrimaryButton(
            text = "Iniciar sesión",
            activeRole = role,
            onClick = viewModel::login,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "¿No tienes cuenta? ", color = TextGray, fontSize = 14.sp)
            Text(
                text = "Regístrate",
                color = if (role == "artista") ArtistaGradientStart else InteresadoGradientStart,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { 
                    viewModel.setAddingRole(false)
                    onNavigateToRegister() 
                }
            )
        }
    }
}
