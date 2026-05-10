package com.antigravity.swart.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.swart.presentation.theme.*

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    activeRole: String = "interesado",
    enabled: Boolean = true
) {
    val activeGradient = if (activeRole == "artista") {
        Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))
    }

    // A simple implementation of the field
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = if (enabled) TextGray else TextGray.copy(alpha = 0.5f))
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) CardBackground else CardBackground.copy(alpha = 0.5f)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = BorderColor,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = TextWhite
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun RoleSwitch(
    activeRole: String,
    onRoleChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoleCard(
            modifier = Modifier.weight(1f),
            title = "Interesad@",
            desc = "Descubre arte",
            icon = Icons.Default.Visibility,
            isActive = activeRole == "interesado",
            role = "interesado",
            enabled = enabled,
            onClick = { if (enabled) onRoleChange("interesado") }
        )
        
        RoleCard(
            modifier = Modifier.weight(1f),
            title = "Artista",
            desc = "Publica obras",
            icon = Icons.Default.Palette,
            isActive = activeRole == "artista",
            role = "artista",
            enabled = enabled,
            onClick = { if (enabled) onRoleChange("artista") }
        )
    }
}

@Composable
fun RoleCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: ImageVector,
    isActive: Boolean,
    role: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val gradient = if (role == "artista") {
        Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))
    }

    val shadowColor = if (role == "artista") ArtistaShadow else InteresadoShadow

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color.Transparent else if (enabled) CardBackground else CardBackground.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (isActive) Color.Transparent else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .then(
                if (isActive) Modifier.background(gradient) else Modifier
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) TextWhite else if (enabled) TextWhite else TextWhite.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = if (isActive) TextWhite else if (enabled) TextWhite else TextWhite.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = desc,
                color = if (isActive) TextWhite.copy(alpha = 0.9f) else TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    activeRole: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val gradient = if (activeRole == "artista") {
        Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(gradient, RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(24.dp))
        } else {
            Text(text = text, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
