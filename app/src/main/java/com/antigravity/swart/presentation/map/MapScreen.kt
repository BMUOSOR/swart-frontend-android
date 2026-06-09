package com.antigravity.swart.presentation.map

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.SolidColor
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
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat

fun createMarkerBitmap(context: Context, tag: String, scale: Float): Bitmap {
    val size = 200 // Increased resolution for sharper icons
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Scale marker drawing based on the passed scale factor
    canvas.scale(scale, scale, size / 2f, size / 2f)
    
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
    
    // Draw Manual Vector Icon (Visual transparency using the same gradient)
    paint.shader = null
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.FILL
    
    val centerX = size / 2f
    val centerY = size / 2f
    val s = size / 55f
    val offset = 12f
    
    val mainPath = android.graphics.Path()
    val holePath = android.graphics.Path()
    
    when (tag.lowercase()) {
        "fotografía" -> {
            // Frame
            mainPath.moveTo(centerX + (21f - offset) * s, centerY + (19f - offset) * s)
            mainPath.lineTo(centerX + (21f - offset) * s, centerY + (5f - offset) * s)
            mainPath.cubicTo(centerX + (21f - offset) * s, centerY + (3.9f - offset) * s, centerX + (20.1f - offset) * s, centerY + (3f - offset) * s, centerX + (19f - offset) * s, centerY + (3f - offset) * s)
            mainPath.lineTo(centerX + (5f - offset) * s, centerY + (3f - offset) * s)
            mainPath.cubicTo(centerX + (3.9f - offset) * s, centerY + (3f - offset) * s, centerX + (3f - offset) * s, centerY + (3.9f - offset) * s, centerX + (3f - offset) * s, centerY + (5f - offset) * s)
            mainPath.lineTo(centerX + (3f - offset) * s, centerY + (19f - offset) * s)
            mainPath.cubicTo(centerX + (3f - offset) * s, centerY + (20.1f - offset) * s, centerX + (3.9f - offset) * s, centerY + (21f - offset) * s, centerX + (5f - offset) * s, centerY + (21f - offset) * s)
            mainPath.lineTo(centerX + (19f - offset) * s, centerY + (21f - offset) * s)
            mainPath.cubicTo(centerX + (20.1f - offset) * s, centerY + (21f - offset) * s, centerX + (21f - offset) * s, centerY + (20.1f - offset) * s, centerX + (21f - offset) * s, centerY + (19f - offset) * s)
            mainPath.close()
            
            // Mountain hole
            holePath.moveTo(centerX + (8.5f - offset) * s, centerY + (13.5f - offset) * s)
            holePath.lineTo(centerX + (5f - offset) * s, centerY + (18f - offset) * s)
            holePath.lineTo(centerX + (19f - offset) * s, centerY + (18f - offset) * s)
            holePath.lineTo(centerX + (14.5f - offset) * s, centerY + (12f - offset) * s)
            holePath.lineTo(centerX + (11f - offset) * s, centerY + (16.51f - offset) * s)
            holePath.close()
        }
        "escultura" -> {
            // Museum
            mainPath.addRect(centerX + (2f - offset) * s, centerY + (19f - offset) * s, centerX + (21f - offset) * s, centerY + (22f - offset) * s, android.graphics.Path.Direction.CW)
            mainPath.addRect(centerX + (4f - offset) * s, centerY + (10f - offset) * s, centerX + (7f - offset) * s, centerY + (17f - offset) * s, android.graphics.Path.Direction.CW)
            mainPath.addRect(centerX + (10f - offset) * s, centerY + (10f - offset) * s, centerX + (13f - offset) * s, centerY + (17f - offset) * s, android.graphics.Path.Direction.CW)
            mainPath.addRect(centerX + (16f - offset) * s, centerY + (10f - offset) * s, centerX + (19f - offset) * s, centerY + (17f - offset) * s, android.graphics.Path.Direction.CW)
            mainPath.moveTo(centerX + (11.5f - offset) * s, centerY + (2f - offset) * s)
            mainPath.lineTo(centerX + (2f - offset) * s, centerY + (6f - offset) * s)
            mainPath.lineTo(centerX + (2f - offset) * s, centerY + (8f - offset) * s)
            mainPath.lineTo(centerX + (21f - offset) * s, centerY + (8f - offset) * s)
            mainPath.lineTo(centerX + (21f - offset) * s, centerY + (6f - offset) * s)
            mainPath.close()
        }
        else -> {
            // Palette
            mainPath.moveTo(centerX + (12f - offset) * s, centerY + (2f - offset) * s)
            mainPath.cubicTo(centerX + (6.48f - offset) * s, centerY + (2f - offset) * s, centerX + (2f - offset) * s, centerY + (6.48f - offset) * s, centerX + (2f - offset) * s, centerY + (12f - offset) * s)
            mainPath.cubicTo(centerX + (2f - offset) * s, centerY + (17.52f - offset) * s, centerX + (6.48f - offset) * s, centerY + (22f - offset) * s, centerX + (12f - offset) * s, centerY + (22f - offset) * s)
            mainPath.cubicTo(centerX + (12.83f - offset) * s, centerY + (22f - offset) * s, centerX + (13.5f - offset) * s, centerY + (21.33f - offset) * s, centerX + (13.5f - offset) * s, centerY + (20.5f - offset) * s)
            mainPath.cubicTo(centerX + (13.5f - offset) * s, centerY + (20.11f - offset) * s, centerX + (13.35f - offset) * s, centerY + (19.76f - offset) * s, centerX + (13.11f - offset) * s, centerY + (19.49f - offset) * s)
            mainPath.cubicTo(centerX + (12.88f - offset) * s, centerY + (19.23f - offset) * s, centerX + (12.73f - offset) * s, centerY + (18.88f - offset) * s, centerX + (12.73f - offset) * s, centerY + (18.5f - offset) * s)
            mainPath.cubicTo(centerX + (12.73f - offset) * s, centerY + (17.67f - offset) * s, centerX + (13.4f - offset) * s, centerY + (17f - offset) * s, centerX + (14.23f - offset) * s, centerY + (17f - offset) * s)
            mainPath.lineTo(centerX + (16f - offset) * s, centerY + (17f - offset) * s)
            mainPath.cubicTo(centerX + (18.76f - offset) * s, centerY + (17f - offset) * s, centerX + (21f - offset) * s, centerY + (14.76f - offset) * s, centerX + (21f - offset) * s, centerY + (12f - offset) * s)
            mainPath.cubicTo(centerX + (21f - offset) * s, centerY + (6.48f - offset) * s, centerX + (16.52f - offset) * s, centerY + (2f - offset) * s, centerX + (12f - offset) * s, centerY + (2f - offset) * s)
            mainPath.close()
            
            // Dots
            holePath.addCircle(centerX + (6.5f - offset) * s, centerY + (10.5f - offset) * s, 1.8f * s, android.graphics.Path.Direction.CW)
            holePath.addCircle(centerX + (9.5f - offset) * s, centerY + (6.5f - offset) * s, 1.8f * s, android.graphics.Path.Direction.CW)
            holePath.addCircle(centerX + (14.5f - offset) * s, centerY + (6.5f - offset) * s, 1.8f * s, android.graphics.Path.Direction.CW)
            holePath.addCircle(centerX + (17.5f - offset) * s, centerY + (10.5f - offset) * s, 1.8f * s, android.graphics.Path.Direction.CW)
        }
    }
    
    // 1. Draw the main shape in White
    paint.shader = null
    paint.color = 0xFFFFFFFF.toInt()
    canvas.drawPath(mainPath, paint)
    
    // 2. Draw the "holes" using the SAME gradient as the background
    if (!holePath.isEmpty) {
        paint.shader = gradient
        canvas.drawPath(holePath, paint)
    }
    
    return bitmap
}

fun createEmptyBalizaMarker(context: Context): Bitmap {
    val size = 140
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Shadow
    paint.color = 0x33000000
    canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)

    // Gray outer circle
    paint.color = 0xFF6B7280.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, paint)

    // Dark inner circle
    paint.color = 0xFF1F2937.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, paint)

    // Lighter gray middle circle
    paint.color = 0xFF9CA3AF.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 3.5f, paint)

    // White "+" symbol
    paint.shader = null
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = size / 15f
    paint.strokeCap = Paint.Cap.ROUND
    val r = size / 8f
    canvas.drawLine(size / 2f - r, size / 2f, size / 2f + r, size / 2f, paint)
    canvas.drawLine(size / 2f, size / 2f - r, size / 2f, size / 2f + r, paint)

    return bitmap
}

@Composable
fun MapScreen(
    userType: UserType = UserType.GENERAL,
    onNavigateHome: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateToObras: () -> Unit = {},
    onNavigateToMensajes: () -> Unit = {},
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToCreateExhibition: (lat: Double, lon: Double, balizaId: Long) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {},
    exhibitionIdToSelect: Long = -1L,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val isArtist = userType == UserType.ARTIST

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lastCenteredPinId by remember { mutableStateOf<Long?>(null) }

    // Recargar el mapa cada vez que la pantalla vuelve al primer plano
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getMapPins()
                viewModel.loadEmptyBalizas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Dialog state for Google Maps redirect
    var showGoogleMapsDialog by remember { mutableStateOf(false) }
    var pendingGoogleMapsPin by remember { mutableStateOf<MapPin?>(null) }
    var hasPromptedForNavigation by remember(exhibitionIdToSelect) { mutableStateOf(false) }
    
    // Prepare official Painters
    val palettePainter = rememberVectorPainter(Icons.Default.Palette)
    val photoPainter = rememberVectorPainter(Icons.Default.Image)
    val sculpturePainter = rememberVectorPainter(Icons.Default.AccountBalance)
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(exhibitionIdToSelect) {
        if (exhibitionIdToSelect != -1L) {
            viewModel.selectExhibition(exhibitionIdToSelect)
        }
    }

    LaunchedEffect(exhibitionIdToSelect, uiState.filteredPins) {
        if (exhibitionIdToSelect != -1L && !hasPromptedForNavigation && uiState.filteredPins.isNotEmpty()) {
            val targetPin = uiState.filteredPins.find { it.idExposicion == exhibitionIdToSelect }
            if (targetPin != null) {
                pendingGoogleMapsPin = targetPin
                showGoogleMapsDialog = true
                hasPromptedForNavigation = true
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (!uiState.isPlacingMode) {
                FloatingActionButton(
                    onClick = { viewModel.togglePlacingMode() },
                    containerColor = Color(0xFF374151),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocation,
                        contentDescription = "Colocar baliza vacía"
                    )
                }
            }
        },
        bottomBar = {
            SwartBottomNav(
                userType = userType,
                currentRoute = "mapa",
                onNavigate = {
                    when (it) {
                        "home" -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "obras" -> onNavigateToObras()
                        "mensajes" -> onNavigateToMensajes()
                        "favoritos" -> onNavigateToFavoritos()
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
                            arrayOf(
                                "https://a.basemaps.cartocdn.com/dark_all/",
                                "https://b.basemaps.cartocdn.com/dark_all/",
                                "https://c.basemaps.cartocdn.com/dark_all/"
                            )
                        )

                        tileProvider.tileSource = darkTileSource
                        

                        
                        // Center on Madrid initially
                        controller.setZoom(13.0)
                        controller.setCenter(GeoPoint(40.4168, -3.7038))
                        
                        // Hide default zoom buttons for cleaner UI
                        setBuiltInZoomControls(false)
                        
                        // Disable standard info windows (we use our own card)
                        InfoWindow.closeAllInfoWindowsOn(this)
                    }.also { mapViewRef = it }
                },
                update = { mapView ->
                    mapViewRef = mapView
                    mapView.overlays.clear()

                    // Exaggerate differences proportionally relative to current matches (feature scaling)
                    val minMatch = uiState.filteredPins.minOfOrNull { it.match } ?: 50
                    val maxMatch = uiState.filteredPins.maxOfOrNull { it.match } ?: 100

                    val effectiveMin = minOf(minMatch.toFloat(), maxMatch.toFloat() - 15f)
                    val range = maxMatch.toFloat() - effectiveMin

                    // Helper: añadir marcadores de exposiciones
                    fun addExhibitionMarkers() {
                        uiState.filteredPins.forEach { pin ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(pin.lat, pin.lon)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            val factor = if (range > 0f) ((pin.match - effectiveMin) / range).coerceIn(0f, 1f) else 0.5f
                            val scale = 0.5f + factor * 0.7f
                            val markerAlpha = 0.3f + factor * 0.7f
                            val markerBitmap = createMarkerBitmap(context, pin.mainTag, scale)
                            marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, markerBitmap)
                            marker.alpha = markerAlpha
                            marker.setOnMarkerClickListener { _, _ ->
                                viewModel.onPinClick(pin)
                                true
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    // Helper: añadir balizas vacías (marcadores grises)
                    val emptyBalizaBitmap = createEmptyBalizaMarker(context)
                    fun addEmptyBalizaMarkers() {
                        uiState.emptyBalizas.forEach { baliza ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(baliza.lat, baliza.lon)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, emptyBalizaBitmap)
                            marker.alpha = 0.85f
                            marker.setOnMarkerClickListener { _, _ ->
                                viewModel.onEmptyBalizaClick(baliza)
                                true
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    // Artista: balizas vacías encima de exposiciones
                    // Interesado: balizas vacías debajo de exposiciones
                    if (isArtist) {
                        addExhibitionMarkers()
                        addEmptyBalizaMarkers()  // encima
                    } else {
                        addEmptyBalizaMarkers()  // debajo
                        addExhibitionMarkers()
                    }

                    // Center and zoom if selectedPin changes
                    uiState.selectedPin?.let { pin ->
                        if (lastCenteredPinId != pin.idExposicion) {
                            lastCenteredPinId = pin.idExposicion
                            val point = GeoPoint(pin.lat, pin.lon)
                            mapView.controller.animateTo(point)
                            mapView.controller.setZoom(15.0)
                        }
                    } ?: run {
                        lastCenteredPinId = null
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
                FunctionalSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onFilterClick = { viewModel.toggleFilterSheet(true) }
                )
            }

            // Selected Exhibition Card Overlay (role-aware)
            if (!uiState.isPlacingMode) {
                uiState.selectedPin?.let { pin ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        ExhibitionMapCard(
                            pin = pin,
                            isClickable = !isArtist,
                            onClick = { if (!isArtist) onNavigateToDetail(pin.idExposicion) }
                        )
                    }
                }

                // Empty Baliza Card Overlay
                uiState.selectedEmptyBaliza?.let { baliza ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        if (isArtist) {
                            ArtistEmptyBalizaCard(
                                baliza = baliza,
                                address = uiState.selectedEmptyBalizaAddress,
                                isLoadingAddress = uiState.isLoadingBalizaAddress,
                                onCreateExhibition = {
                                    viewModel.dismissEmptyBaliza()
                                    onNavigateToCreateExhibition(baliza.lat, baliza.lon, baliza.id)
                                },
                                onDelete = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        } else {
                            InteresadoEmptyBalizaCard(
                                address = uiState.selectedEmptyBalizaAddress,
                                isLoadingAddress = uiState.isLoadingBalizaAddress,
                                onDelete = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        }
                    }
                }
            }

            // Placing Mode Overlay
            if (uiState.isPlacingMode) {
                // Semi-transparent top banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 96.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xCC1F2937),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Mueve el mapa para posicionar la baliza",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Crosshair at center
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                ) {
                    // Horizontal line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.9f))
                            .align(Alignment.Center)
                    )
                    // Vertical line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.9f))
                            .align(Alignment.Center)
                    )
                    // Center dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6B7280))
                            .align(Alignment.Center)
                    )
                }

                // Confirm / Cancel buttons at bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.togglePlacingMode() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B7280))
                    ) {
                        Text("Cancelar", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val center = mapViewRef?.mapCenter
                            if (center != null) {
                                viewModel.placeEmptyBaliza(center.latitude, center.longitude)
                            }
                        },
                        enabled = !uiState.isCreatingEmptyBaliza,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))
                    ) {
                        if (uiState.isCreatingEmptyBaliza) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Colocar aquí", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = InteresadoGradientStart,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Filter Bottom Sheet
            if (uiState.isFilterSheetVisible) {
                FilterBottomSheet(
                    uiState = uiState,
                    onDismiss = { viewModel.toggleFilterSheet(false) },
                    onToggleTag = viewModel::onToggleTag,
                    onPriceChange = viewModel::onPriceChange,
                    onDateRangeChange = viewModel::onDateRangeChange
                )
            }

            // Google Maps Redirect Dialog
            if (showGoogleMapsDialog && pendingGoogleMapsPin != null) {
                val pin = pendingGoogleMapsPin!!
                AlertDialog(
                    onDismissRequest = { showGoogleMapsDialog = false },
                    title = { Text(text = "Ver en Google Maps", color = Color.White) },
                    text = { Text(text = "¿Deseas abrir la ubicación de \"${pin.titulo}\" en la aplicación de Google Maps?", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showGoogleMapsDialog = false
                                val gmmIntentUri = android.net.Uri.parse("geo:${pin.lat},${pin.lon}?q=${android.net.Uri.encode(pin.titulo + ", " + pin.galeria)}")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Fallback if Google Maps app is not installed
                                    val mapIntentFallback = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    try {
                                        context.startActivity(mapIntentFallback)
                                    } catch (ex: Exception) {
                                        // Ignore
                                    }
                                }
                            }
                        ) {
                            Text("Sí", color = ArtistaGradientStart)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGoogleMapsDialog = false }) {
                            Text("No", color = TextGray)
                        }
                    },
                    containerColor = CardBackground,
                    textContentColor = Color.LightGray,
                    titleContentColor = Color.White
                )
            }
        }
    }
}

@Composable
fun FunctionalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    // Local TextFieldValue preserva la composición IME para ñ, acentos, etc.
    var tfv by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) { if (query != tfv.text) tfv = TextFieldValue(query) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = CardBackground.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (tfv.text.isEmpty()) {
                    Text(
                        text = "Galerías, obras o artistas",
                        color = TextGray,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = tfv,
                    onValueChange = { tfv = it; onQueryChange(it.text) },
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(InteresadoGradientStart),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (tfv.text.isNotEmpty()) {
                IconButton(onClick = { tfv = TextFieldValue(""); onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd)))
                    .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: MapUiState,
    onDismiss: () -> Unit,
    onToggleTag: (String) -> Unit,
    onPriceChange: (Float) -> Unit,
    onDateRangeChange: (Long?, Long?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = uiState.startDate,
        initialSelectedEndDateMillis = uiState.endDate
    )
    
    // Sync state when uiState changes (e.g. cleared)
    LaunchedEffect(uiState.startDate, uiState.endDate) {
        if (uiState.startDate != datePickerState.selectedStartDateMillis || 
            uiState.endDate != datePickerState.selectedEndDateMillis) {
            datePickerState.setSelection(uiState.startDate, uiState.endDate)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateRangeChange(
                        datePickerState.selectedStartDateMillis,
                        datePickerState.selectedEndDateMillis
                    )
                    showDatePicker = false
                }) {
                    Text("OK", color = InteresadoGradientStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextGray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CardBackground
            )
        ) {
            DateRangePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Selecciona el periodo",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        color = TextGray,
                        fontSize = 14.sp
                    )
                },
                headline = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                        val startText = datePickerState.selectedStartDateMillis?.let { sdf.format(Date(it)) } ?: "Inicio"
                        val endText = datePickerState.selectedEndDateMillis?.let { sdf.format(Date(it)) } ?: "Fin"
                        
                        Text(
                            text = "$startText — $endText",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = CardBackground,
                    titleContentColor = TextWhite,
                    headlineContentColor = TextWhite,
                    weekdayContentColor = TextGray,
                    dayContentColor = TextWhite,
                    selectedDayContainerColor = InteresadoGradientStart,
                    selectedDayContentColor = TextWhite,
                    todayContentColor = InteresadoGradientStart,
                    todayDateBorderColor = InteresadoGradientStart,
                    dayInSelectionRangeContainerColor = InteresadoGradientStart.copy(alpha = 0.2f),
                    dayInSelectionRangeContentColor = TextWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextGray.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Filtros",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Discipline Section
            FilterSectionTitle("Disciplina")
            val tags = listOf("Pintura", "Escultura", "Fotografía")
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    FilterChipItem(
                        text = tag,
                        isSelected = uiState.selectedTags.contains(tag),
                        onClick = { onToggleTag(tag) }
                    )
                }
            }

            // Price Section
            FilterSectionTitle("Precio Máximo: ${if (uiState.maxPrice >= 100f) "Cualquiera" else "${uiState.maxPrice.toInt()}€"}")
            Slider(
                value = uiState.maxPrice,
                onValueChange = onPriceChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = InteresadoGradientStart,
                    activeTrackColor = InteresadoGradientStart,
                    inactiveTrackColor = TextGray.copy(alpha = 0.2f)
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Date Section
            FilterSectionTitle("Rango de Fechas")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true },
                color = CardBackground.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = InteresadoGradientStart)
                    Spacer(modifier = Modifier.width(12.dp))
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val dateText = when {
                        uiState.startDate != null && uiState.endDate != null -> {
                            "${sdf.format(Date(uiState.startDate))} - ${sdf.format(Date(uiState.endDate))}"
                        }
                        uiState.startDate != null -> {
                            "Desde ${sdf.format(Date(uiState.startDate))}"
                        }
                        else -> "Seleccionar rango"
                    }
                    Text(
                        text = dateText,
                        color = if (uiState.startDate != null) TextWhite else TextGray,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.startDate != null) {
                        IconButton(onClick = { onDateRangeChange(null, null) }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aplicar Filtros", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        color = TextWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color.Transparent else CardBackground.copy(alpha = 0.8f),
        border = if (isSelected) null else BorderStroke(1.dp, TextGray.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd)))
                    else Modifier
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) TextWhite else TextGray,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ExhibitionMapCard(
    pin: MapPin,
    isClickable: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
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

@Composable
fun ArtistEmptyBalizaCard(
    baliza: com.antigravity.swart.domain.model.EmptyBaliza,
    address: String?,
    isLoadingAddress: Boolean,
    onCreateExhibition: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.97f),
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Baliza vacía", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (isLoadingAddress) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(10.dp), color = TextGray, strokeWidth = 1.5.dp)
                                Text("Obteniendo dirección...", color = TextGray, fontSize = 11.sp)
                            }
                        } else {
                            Text(
                                text = address ?: "${"%.5f".format(baliza.lat)}, ${"%.5f".format(baliza.lon)}",
                                color = TextGray, fontSize = 11.sp, maxLines = 2
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Button(
                onClick = onCreateExhibition,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Crear exposición aquí", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun InteresadoEmptyBalizaCard(
    address: String?,
    isLoadingAddress: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.97f),
        tonalElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF374151)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Baliza vacía", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (isLoadingAddress) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(10.dp), color = TextGray, strokeWidth = 1.5.dp)
                        Text("Obteniendo dirección...", color = TextGray, fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = address ?: "Sin exposición asignada todavía",
                        color = TextGray, fontSize = 13.sp, maxLines = 2
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray, modifier = Modifier.size(18.dp))
            }
        }
    }
}
