package com.antigravity.swart.presentation.exhibitions

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun createTempImageUri(context: Context): Uri {
    val tempFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
    if (!tempFile.parentFile.exists()) tempFile.parentFile.mkdirs()
    return FileProvider.getUriForFile(
        context,
        "com.antigravity.swart.fileprovider",
        tempFile
    )
}

fun uriToMultipartBodyPart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val fileName = "upload_${System.currentTimeMillis()}.$extension"
        val mediaType = mimeType.toMediaTypeOrNull()
        val requestBody = bytes.toRequestBody(mediaType)
        MultipartBody.Part.createFormData(partName, fileName, requestBody)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ImageSourceSelectorDialog(
    onDismiss: () -> Unit,
    onGallerySelect: () -> Unit,
    onCameraSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161925), // CardBg
        title = {
            Text(
                "Seleccionar imagen",
                color = Color(0xFFE8E8F0),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = {
                        onGallerySelect()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Galería de fotos", color = Color(0xFFEC4899), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                TextButton(
                    onClick = {
                        onCameraSelect()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hacer foto (Cámara)", color = Color(0xFFEC4899), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF8B8FA8))
            }
        }
    )
}
