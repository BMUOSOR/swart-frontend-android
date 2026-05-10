package com.antigravity.swart.presentation.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.core.content.ContextCompat

fun createMarkerBitmap(context: Context, tag: String): Bitmap {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Colors based on discipline
    val startColor = if (tag.lowercase() == "escultura") 0xFFEC4899.toInt() else 0xFF6366F1.toInt()
    val endColor = if (tag.lowercase() == "escultura") 0xFF8B5CF6.toInt() else 0xFF3B82F6.toInt()
    
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Draw Glow/Shadow
    paint.color = 0x44000000
    canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)
    
    // Draw Gradient Circle
    val gradient = LinearGradient(0f, 0f, size.toFloat(), size.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
    paint.shader = gradient
    canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, paint)
    
    // Draw Inner Dark Circle (Ring effect)
    paint.shader = null
    paint.color = 0xFF13131A.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, paint)
    
    // Draw Middle Gradient Circle again
    paint.shader = gradient
    canvas.drawCircle(size / 2f, size / 2f, size / 3.2f, paint)
    
    // Draw Icon
    val isPainting = tag.lowercase() != "fotografía" && tag.lowercase() != "escultura"
    
    if (isPainting) {
        // Draw the EXACT Material Design Palette icon shape manually
        paint.shader = null
        paint.color = Color.White.toArgb()
        paint.style = Paint.Style.FILL
        
        val centerX = size / 2f
        val centerY = size / 2f
        val s = size / 42f // Scaled down to leave more padding (approx 55% smaller)
        
        // Translate to center and scale
        val path = android.graphics.Path().apply {
            // This is a simplified version of the official Material Palette Path
            moveTo(centerX + 0f * s, centerY - 9f * s)
            cubicTo(centerX - 4.97f * s, centerY - 9f * s, centerX - 9f * s, centerY - 4.97f * s, centerX - 9f * s, centerY + 0f * s)
            cubicTo(centerX - 9f * s, centerY + 4.97f * s, centerX - 4.97f * s, centerY + 9f * s, centerX + 0f * s, centerY + 9f * s)
            cubicTo(centerX + 0.83f * s, centerY + 9f * s, centerX + 1.5f * s, centerY + 8.33f * s, centerX + 1.5f * s, centerY + 7.5f * s)
            cubicTo(centerX + 1.5f * s, centerY + 7.11f * s, centerX + 1.35f * s, centerY + 6.76f * s, centerX + 1.11f * s, centerY + 6.5f * s)
            cubicTo(centerX + 0.88f * s, centerY + 6.23f * s, centerX + 0.73f * s, centerY + 5.88f * s, centerX + 0.73f * s, centerY + 5.5f * s)
            cubicTo(centerX + 0.73f * s, centerY + 4.67f * s, centerX + 1.4f * s, centerY + 4f * s, centerX + 2.23f * s, centerY + 4f * s)
            lineTo(centerX + 4f * s, centerY + 4f * s)
            cubicTo(centerX + 6.76f * s, centerY + 4f * s, centerX + 9f * s, centerY + 1.76f * s, centerX + 9f * s, centerY - 1f * s)
            cubicTo(centerX + 9f * s, centerY - 5.42f * s, centerX + 4.97f * s, centerY - 9f * s, centerX + 0f * s, centerY - 9f * s)
            close()
            
            // Add dots as CCW circles to "cut" them out of the path (making them transparent)
            addCircle(centerX - 5.5f * s, centerY - 1.5f * s, 1.5f * s, android.graphics.Path.Direction.CCW)
            addCircle(centerX - 2.5f * s, centerY - 5.5f * s, 1.5f * s, android.graphics.Path.Direction.CCW)
            addCircle(centerX + 2.5f * s, centerY - 5.5f * s, 1.5f * s, android.graphics.Path.Direction.CCW)
            addCircle(centerX + 5.5f * s, centerY - 1.5f * s, 1.5f * s, android.graphics.Path.Direction.CCW)
        }
        canvas.drawPath(path, paint)
    } else {
        val iconRes = when (tag.lowercase()) {
            "fotografía" -> android.R.drawable.ic_menu_camera
            "escultura" -> android.R.drawable.ic_menu_gallery
            else -> android.R.drawable.ic_menu_gallery
        }
        
        val drawable = ContextCompat.getDrawable(context, iconRes)
        drawable?.let {
            it.setTint(0xFFFFFFFF.toInt())
            val iconSize = (size / 2.5).toInt()
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2
            it.setBounds(left, top, left + iconSize, top + iconSize)
            it.draw(canvas)
        }
    }
    
    return bitmap
}

@Composable
fun MapScreen(
    userType: UserType = UserType.GENERAL,
    onNavigateHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            SwartBottomNav(
                userType = userType,
                currentRoute = "mapa",
                onNavigate = {
                    when (it) {
                        "home" -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "perfil" -> onLogout()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Real OpenStreetMap (Functional)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        
                        // Dark Tiles Style (CartoDB Dark Matter)
                        val darkTileSource = XYTileSource(
                            "CartoDB_DarkMatter",
                            0, 19, 256, ".png",
                            arrayOf("https://a.basemaps.cartocdn.com/dark_all/", 
                                    "https://b.basemaps.cartocdn.com/dark_all/", 
                                    "https://c.basemaps.cartocdn.com/dark_all/")
                        )
                        tileProvider.tileSource = darkTileSource
                        
                        // Center on Madrid initially
                        controller.setZoom(13.0)
                        controller.setCenter(GeoPoint(40.4168, -3.7038))
                        
                        // Hide default zoom buttons for cleaner UI
                        setBuiltInZoomControls(false)
                        
                        // Disable standard info windows (we use our own card)
                        InfoWindow.closeAllInfoWindowsOn(this)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    uiState.pins.forEach { pin ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(pin.lat, pin.lon)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        
                        // Create custom premium icon
                        val iconRes = when (pin.mainTag.lowercase()) {
                            "fotografía" -> android.R.drawable.ic_menu_camera
                            "escultura" -> android.R.drawable.ic_menu_gallery
                            else -> android.R.drawable.ic_menu_edit
                        }
                        
                        val markerBitmap = createMarkerBitmap(context, pin.mainTag)
                        marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, markerBitmap)
                        
                        marker.setOnMarkerClickListener { _, _ ->
                            viewModel.onPinClick(pin)
                            true
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }
            )

            // Search Bar Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                SearchBarMock()
            }

            // Selected Exhibition Card Overlay
            uiState.selectedPin?.let { pin ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    ExhibitionMapCard(
                        pin = pin,
                        onClick = { onNavigateToDetail(pin.idExposicion) }
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = InteresadoGradientStart,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SearchBarMock() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Galerías, obras o artistas",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TextWhite, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ExhibitionMapCard(
    pin: MapPin,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.95f),
        tonalElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pin.imagen,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pin.titulo,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = pin.galeria,
                    color = TextGray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "${pin.match}% match",
                        color = InteresadoGradientStart,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = " · ${pin.distancia}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Discipline Icon
            val (icon, gradient) = when (pin.mainTag.lowercase()) {
                "fotografía" -> Icons.Default.Image to listOf(InteresadoGradientStart, InteresadoGradientEnd)
                "escultura" -> Icons.Default.AccountBalance to listOf(ArtistaGradientStart, ArtistaGradientEnd)
                else -> Icons.Default.Palette to listOf(InteresadoGradientStart, InteresadoGradientEnd)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(22.dp))
            }
        }
    }
}
