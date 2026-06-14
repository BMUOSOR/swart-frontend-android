package com.antigravity.swart.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.antigravity.swart.domain.model.MapPin
import com.antigravity.swart.presentation.components.SwartBottomNav
import com.antigravity.swart.presentation.components.UserType
import com.antigravity.swart.presentation.theme.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillExtrusionLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.bindgen.Value
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.*


// --------------------------------------------------------------------------
// Marker bitmap helpers (identical visual to the original osmdroid version)
// --------------------------------------------------------------------------
fun createMarkerBitmap(context: Context, tag: String, scale: Float): Bitmap {
    val size = 200
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.scale(scale, scale, size / 2f, size / 2f)

    val startColor = if (tag.lowercase() == "escultura") 0xFFEC4899.toInt() else 0xFF6366F1.toInt()
    val endColor   = if (tag.lowercase() == "escultura") 0xFF8B5CF6.toInt() else 0xFF3B82F6.toInt()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val gradient = LinearGradient(0f, 0f, size.toFloat(), size.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)

    paint.color = 0x44000000
    canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)
    paint.shader = gradient
    canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, paint)
    paint.shader = null; paint.color = 0xFF13131A.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, paint)
    paint.shader = gradient
    canvas.drawCircle(size / 2f, size / 2f, size / 3.2f, paint)
    paint.shader = null; paint.color = 0xFFFFFFFF.toInt(); paint.style = Paint.Style.FILL

    val cx = size / 2f; val cy = size / 2f; val s = size / 55f; val off = 12f
    val main = android.graphics.Path(); val hole = android.graphics.Path()
    when (tag.lowercase()) {
        "fotografía" -> {
            main.moveTo(cx+(21f-off)*s, cy+(19f-off)*s); main.lineTo(cx+(21f-off)*s, cy+(5f-off)*s)
            main.cubicTo(cx+(21f-off)*s, cy+(3.9f-off)*s, cx+(20.1f-off)*s, cy+(3f-off)*s, cx+(19f-off)*s, cy+(3f-off)*s)
            main.lineTo(cx+(5f-off)*s, cy+(3f-off)*s)
            main.cubicTo(cx+(3.9f-off)*s, cy+(3f-off)*s, cx+(3f-off)*s, cy+(3.9f-off)*s, cx+(3f-off)*s, cy+(5f-off)*s)
            main.lineTo(cx+(3f-off)*s, cy+(19f-off)*s)
            main.cubicTo(cx+(3f-off)*s, cy+(20.1f-off)*s, cx+(3.9f-off)*s, cy+(21f-off)*s, cx+(5f-off)*s, cy+(21f-off)*s)
            main.lineTo(cx+(19f-off)*s, cy+(21f-off)*s)
            main.cubicTo(cx+(20.1f-off)*s, cy+(21f-off)*s, cx+(21f-off)*s, cy+(20.1f-off)*s, cx+(21f-off)*s, cy+(19f-off)*s)
            main.close()
            hole.moveTo(cx+(8.5f-off)*s, cy+(13.5f-off)*s); hole.lineTo(cx+(5f-off)*s, cy+(18f-off)*s)
            hole.lineTo(cx+(19f-off)*s, cy+(18f-off)*s); hole.lineTo(cx+(14.5f-off)*s, cy+(12f-off)*s)
            hole.lineTo(cx+(11f-off)*s, cy+(16.51f-off)*s); hole.close()
        }
        "escultura" -> {
            main.addRect(cx+(2f-off)*s, cy+(19f-off)*s, cx+(21f-off)*s, cy+(22f-off)*s, android.graphics.Path.Direction.CW)
            main.addRect(cx+(4f-off)*s, cy+(10f-off)*s, cx+(7f-off)*s, cy+(17f-off)*s, android.graphics.Path.Direction.CW)
            main.addRect(cx+(10f-off)*s, cy+(10f-off)*s, cx+(13f-off)*s, cy+(17f-off)*s, android.graphics.Path.Direction.CW)
            main.addRect(cx+(16f-off)*s, cy+(10f-off)*s, cx+(19f-off)*s, cy+(17f-off)*s, android.graphics.Path.Direction.CW)
            main.moveTo(cx+(11.5f-off)*s, cy+(2f-off)*s); main.lineTo(cx+(2f-off)*s, cy+(6f-off)*s)
            main.lineTo(cx+(2f-off)*s, cy+(8f-off)*s); main.lineTo(cx+(21f-off)*s, cy+(8f-off)*s)
            main.lineTo(cx+(21f-off)*s, cy+(6f-off)*s); main.close()
        }
        else -> {
            main.moveTo(cx+(12f-off)*s, cy+(2f-off)*s)
            main.cubicTo(cx+(6.48f-off)*s, cy+(2f-off)*s, cx+(2f-off)*s, cy+(6.48f-off)*s, cx+(2f-off)*s, cy+(12f-off)*s)
            main.cubicTo(cx+(2f-off)*s, cy+(17.52f-off)*s, cx+(6.48f-off)*s, cy+(22f-off)*s, cx+(12f-off)*s, cy+(22f-off)*s)
            main.cubicTo(cx+(12.83f-off)*s, cy+(22f-off)*s, cx+(13.5f-off)*s, cy+(21.33f-off)*s, cx+(13.5f-off)*s, cy+(20.5f-off)*s)
            main.cubicTo(cx+(13.5f-off)*s, cy+(20.11f-off)*s, cx+(13.35f-off)*s, cy+(19.76f-off)*s, cx+(13.11f-off)*s, cy+(19.49f-off)*s)
            main.cubicTo(cx+(12.88f-off)*s, cy+(19.23f-off)*s, cx+(12.73f-off)*s, cy+(18.88f-off)*s, cx+(12.73f-off)*s, cy+(18.5f-off)*s)
            main.cubicTo(cx+(12.73f-off)*s, cy+(17.67f-off)*s, cx+(13.4f-off)*s, cy+(17f-off)*s, cx+(14.23f-off)*s, cy+(17f-off)*s)
            main.lineTo(cx+(16f-off)*s, cy+(17f-off)*s)
            main.cubicTo(cx+(18.76f-off)*s, cy+(17f-off)*s, cx+(21f-off)*s, cy+(14.76f-off)*s, cx+(21f-off)*s, cy+(12f-off)*s)
            main.cubicTo(cx+(21f-off)*s, cy+(6.48f-off)*s, cx+(16.52f-off)*s, cy+(2f-off)*s, cx+(12f-off)*s, cy+(2f-off)*s)
            main.close()
            hole.addCircle(cx+(6.5f-off)*s, cy+(10.5f-off)*s, 1.8f*s, android.graphics.Path.Direction.CW)
            hole.addCircle(cx+(9.5f-off)*s, cy+(6.5f-off)*s, 1.8f*s, android.graphics.Path.Direction.CW)
            hole.addCircle(cx+(14.5f-off)*s, cy+(6.5f-off)*s, 1.8f*s, android.graphics.Path.Direction.CW)
            hole.addCircle(cx+(17.5f-off)*s, cy+(10.5f-off)*s, 1.8f*s, android.graphics.Path.Direction.CW)
        }
    }
    paint.shader = null; paint.color = 0xFFFFFFFF.toInt()
    canvas.drawPath(main, paint)
    if (!hole.isEmpty) { paint.shader = gradient; canvas.drawPath(hole, paint) }
    return bitmap
}

fun createEmptyBalizaMarker(context: Context): Bitmap {
    val size = 140
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0x33000000; canvas.drawCircle(size/2f, size/2f, size/2.1f, paint)
    paint.color = 0xFF6B7280.toInt(); canvas.drawCircle(size/2f, size/2f, size/2.4f, paint)
    paint.color = 0xFF1F2937.toInt(); canvas.drawCircle(size/2f, size/2f, size/2.8f, paint)
    paint.color = 0xFF9CA3AF.toInt(); canvas.drawCircle(size/2f, size/2f, size/3.5f, paint)
    paint.shader = null; paint.color = 0xFFFFFFFF.toInt(); paint.style = Paint.Style.STROKE
    paint.strokeWidth = size/15f; paint.strokeCap = Paint.Cap.ROUND
    val r = size/8f
    canvas.drawLine(size/2f-r, size/2f, size/2f+r, size/2f, paint)
    canvas.drawLine(size/2f, size/2f-r, size/2f, size/2f+r, paint)
    return bitmap
}

fun createGovBalizaMarker(context: Context): Bitmap {
    val size = 140
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0x33000000; canvas.drawCircle(size/2f, size/2f, size/2.1f, paint)
    paint.color = 0xFF10B981.toInt(); canvas.drawCircle(size/2f, size/2f, size/2.4f, paint) // Emerald green
    paint.color = 0xFF047857.toInt(); canvas.drawCircle(size/2f, size/2f, size/2.8f, paint)
    paint.color = 0xFF34D399.toInt(); canvas.drawCircle(size/2f, size/2f, size/3.5f, paint)
    paint.shader = null; paint.color = 0xFFFFFFFF.toInt(); paint.style = Paint.Style.STROKE
    paint.strokeWidth = size/15f; paint.strokeCap = Paint.Cap.ROUND
    // Draw an 'i' for info
    canvas.drawLine(size/2f, size/2f - size/10f, size/2f, size/2f + size/5f, paint)
    canvas.drawPoint(size/2f, size/2f - size/4f, paint)
    return bitmap
}

// --------------------------------------------------------------------------
// MapScreen
// --------------------------------------------------------------------------
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
    val isArtist = userType == UserType.ARTIST

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var lastCenteredPinId by remember { mutableStateOf<Long?>(null) }

    var locationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            locationPermissionGranted = true
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            locationPermissionGranted = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    // Reload on resume
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

    var showGoogleMapsDialog by remember { mutableStateOf(false) }
    var pendingGoogleMapsPin by remember { mutableStateOf<MapPin?>(null) }
    var hasPromptedForNavigation by remember(exhibitionIdToSelect) { mutableStateOf(false) }

    LaunchedEffect(exhibitionIdToSelect) {
        if (exhibitionIdToSelect != -1L) viewModel.selectExhibition(exhibitionIdToSelect)
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
        bottomBar = {
            SwartBottomNav(
                userType = userType,
                currentRoute = "mapa",
                onNavigate = {
                    when (it) {
                        "home"      -> onNavigateHome()
                        "descubrir" -> onNavigateToSwap()
                        "obras"     -> onNavigateToObras()
                        "mensajes"  -> onNavigateToMensajes()
                        "favoritos" -> onNavigateToFavoritos()
                        "perfil"    -> onLogout()
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
            // ----------------------------------------------------------------
            // Mapbox MapView — 3D Dusk (Dust) Style
            // ----------------------------------------------------------------
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).also { mv ->
                        mapViewRef = mv
                        // Disable default compass / scale bar for cleaner UI
                        mv.gestures.rotateEnabled = true
                        mv.gestures.pitchEnabled  = true

                        mv.mapboxMap.loadStyle(Style.STANDARD) { style ->
                            style.setStyleImportConfigProperty("basemap", "lightPreset", com.mapbox.bindgen.Value.valueOf("dusk"))
                        }

                        if (locationPermissionGranted) {
                            mv.location.updateSettings {
                                enabled = true
                                pulsingEnabled = true
                            }
                        }

                        // Initial camera: 3D pitch + Valencia
                        mv.mapboxMap.setCamera(
                            cameraOptions {
                                center(Point.fromLngLat(-0.3763, 39.4699))
                                zoom(13.5)
                                pitch(50.0)
                                bearing(0.0)
                            }
                        )
                    }
                },
                update = { mv ->
                    mapViewRef = mv
                    val mapboxMap = mv.mapboxMap
                    val style = mapboxMap.getStyle() ?: return@AndroidView

                    if (locationPermissionGranted) {
                        mv.location.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                        }
                    }

                    // ── Scale markers by match score ──────────────────────
                    val minMatch = uiState.filteredPins.minOfOrNull { it.match } ?: 50
                    val maxMatch = uiState.filteredPins.maxOfOrNull { it.match } ?: 100
                    val effectiveMin = minOf(minMatch.toFloat(), maxMatch.toFloat() - 15f)
                    val range = maxMatch.toFloat() - effectiveMin

                    // ── Register exhibition bitmaps in style ──────────────
                    uiState.filteredPins.forEach { pin ->
                        val factor = if (range > 0f) ((pin.match - effectiveMin) / range).coerceIn(0f, 1f) else 0.5f
                        val scale  = 0.5f + factor * 0.7f
                        val bmp    = createMarkerBitmap(context, pin.mainTag, scale)
                        val imgId  = "marker-${pin.idExposicion}"
                        if (style.getStyleImage(imgId) == null) {
                            style.addImage(imgId, bmp)
                        }
                    }

                    // Register empty-baliza bitmap
                    val balizaBmp = createEmptyBalizaMarker(context)
                    if (style.getStyleImage("marker-baliza") == null) {
                        style.addImage("marker-baliza", balizaBmp)
                    }

                    // Register gov-baliza bitmap
                    val govBmp = createGovBalizaMarker(context)
                    if (style.getStyleImage("marker-gov") == null) {
                        style.addImage("marker-gov", govBmp)
                    }

                    // ── Build GeoJSON for exhibitions ─────────────────────
                    val exhibFeatures = uiState.filteredPins.map { pin ->
                        Feature.fromGeometry(
                            Point.fromLngLat(pin.lon, pin.lat),
                            null,
                            "exhb-${pin.idExposicion}"
                        ).also {
                            it.addStringProperty("pinId", pin.idExposicion.toString())
                            it.addStringProperty("markerImage", "marker-${pin.idExposicion}")
                        }
                    }
                    val exhibSource = "source-exhibitions"
                    if (style.styleSourceExists(exhibSource)) {
                        style.getSourceAs<com.mapbox.maps.extension.style.sources.generated.GeoJsonSource>(exhibSource)
                            ?.featureCollection(FeatureCollection.fromFeatures(exhibFeatures))
                    } else {
                        style.addSource(geoJsonSource(exhibSource) {
                            featureCollection(FeatureCollection.fromFeatures(exhibFeatures))
                        })
                    }

                    if (!style.styleLayerExists("layer-pins")) {
                        style.addLayer(
                            symbolLayer("layer-pins", exhibSource) {
                                iconImage(get("markerImage"))
                                iconAnchor(IconAnchor.BOTTOM)
                                iconAllowOverlap(true)
                                iconIgnorePlacement(true)
                            }
                        )
                    }

                    // ── Build GeoJSON for empty balizas ───────────────────
                    val balizaFeatures = uiState.emptyBalizas.map { baliza ->
                        Feature.fromGeometry(
                            Point.fromLngLat(baliza.lon, baliza.lat),
                            null,
                            "baliza-${baliza.id}"
                        ).also { it.addStringProperty("balizaId", baliza.id.toString()) }
                    }
                    val balizaSource = "source-balizas"
                    if (style.styleSourceExists(balizaSource)) {
                        style.getSourceAs<com.mapbox.maps.extension.style.sources.generated.GeoJsonSource>(balizaSource)
                            ?.featureCollection(FeatureCollection.fromFeatures(balizaFeatures))
                    } else {
                        style.addSource(geoJsonSource(balizaSource) {
                            featureCollection(FeatureCollection.fromFeatures(balizaFeatures))
                        })
                    }

                    // ── Empty-baliza symbol layer ─────────────────────────
                    val balizaLayer = "layer-balizas"
                    if (!style.styleLayerExists(balizaLayer)) {
                        style.addLayer(
                            symbolLayer(balizaLayer, balizaSource) {
                                iconImage(literal("marker-baliza"))
                                iconAnchor(IconAnchor.CENTER)
                                iconAllowOverlap(true)
                                iconIgnorePlacement(true)
                            }
                        )
                    }

                    // ── Build GeoJSON for gov balizas ───────────────────
                    val govFeatures = uiState.govBalizas.map { baliza ->
                        Feature.fromGeometry(
                            Point.fromLngLat(baliza.lon, baliza.lat),
                            null,
                            "gov-${baliza.id}"
                        ).also { it.addStringProperty("govId", baliza.id.toString()) }
                    }
                    val govSource = "source-gov"
                    if (style.styleSourceExists(govSource)) {
                        style.getSourceAs<com.mapbox.maps.extension.style.sources.generated.GeoJsonSource>(govSource)
                            ?.featureCollection(FeatureCollection.fromFeatures(govFeatures))
                    } else {
                        style.addSource(geoJsonSource(govSource) {
                            featureCollection(FeatureCollection.fromFeatures(govFeatures))
                        })
                    }

                    // ── Gov-baliza symbol layer ─────────────────────────
                    val govLayer = "layer-gov"
                    if (!style.styleLayerExists(govLayer)) {
                        style.addLayer(
                            symbolLayer(govLayer, govSource) {
                                iconImage(literal("marker-gov"))
                                iconAnchor(IconAnchor.CENTER)
                                iconAllowOverlap(true)
                                iconIgnorePlacement(true)
                            }
                        )
                    }

                    // Click handler
                    mv.gestures.addOnMapClickListener { clickPoint ->
                        val screenPoint = mapboxMap.pixelForCoordinate(clickPoint)
                        mapboxMap.queryRenderedFeatures(
                            com.mapbox.maps.RenderedQueryGeometry(com.mapbox.maps.ScreenCoordinate(screenPoint.x, screenPoint.y)),
                            com.mapbox.maps.RenderedQueryOptions(
                                listOf("layer-pins", "layer-balizas", "layer-gov"), null
                            )
                        ) { result ->
                            result.value?.firstOrNull()?.queriedFeature?.feature?.let { feature ->
                                val pinIdStr    = feature.getStringProperty("pinId")
                                val balizaIdStr = feature.getStringProperty("balizaId")
                                val govIdStr    = feature.getStringProperty("govId")
                                if (pinIdStr != null) {
                                    uiState.filteredPins.find { it.idExposicion.toString() == pinIdStr }
                                        ?.let { viewModel.onPinClick(it) }
                                } else if (balizaIdStr != null) {
                                    uiState.emptyBalizas.find { it.id.toString() == balizaIdStr }
                                        ?.let { viewModel.onEmptyBalizaClick(it) }
                                } else if (govIdStr != null) {
                                    viewModel.onGovBalizaClick(govIdStr.toLong())
                                }
                                Unit
                            }
                        }
                        false
                    }

                    // ── Fly to selected pin ───────────────────────────────
                    uiState.selectedPin?.let { pin ->
                        if (lastCenteredPinId != pin.idExposicion) {
                            lastCenteredPinId = pin.idExposicion
                            mapboxMap.flyTo(
                                cameraOptions {
                                    center(Point.fromLngLat(pin.lon, pin.lat))
                                    zoom(16.0)
                                    pitch(55.0)
                                }
                            )
                        }
                    } ?: run { lastCenteredPinId = null }
                }
            )

            // ── Search & Action Overlay ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.End
            ) {
                Spacer(modifier = Modifier.height(72.dp))
                FunctionalSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onFilterClick = { viewModel.toggleFilterSheet(true) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { viewModel.toggleAddressSearch(true) },
                    containerColor = Color(0xFF374151),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Place, contentDescription = "Colocar baliza vacía")
                }
            }

            // ── Selected Exhibition Card ──────────────────────────────────
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

                // ── Empty Baliza Card ─────────────────────────────────────
                uiState.selectedEmptyBaliza?.let { baliza ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        val isOwner = baliza.idPropietario == uiState.currentUserId
                        if (isArtist) {
                            ArtistEmptyBalizaCard(
                                baliza = baliza,
                                address = uiState.selectedEmptyBalizaAddress,
                                isLoadingAddress = uiState.isLoadingBalizaAddress,
                                isOwner = isOwner,
                                onCreateExhibition = {
                                    viewModel.dismissEmptyBaliza()
                                    onNavigateToCreateExhibition(baliza.lat, baliza.lon, baliza.id)
                                },
                                onSendProposal = { viewModel.toggleProposalForm(true) },
                                onDelete  = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        } else {
                            InteresadoEmptyBalizaCard(
                                address = uiState.selectedEmptyBalizaAddress,
                                isLoadingAddress = uiState.isLoadingBalizaAddress,
                                isOwner = isOwner,
                                onDelete  = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        }
                    }
                }
            
            // ── Gov Baliza Overlay ────────────────────────────────────────
            uiState.selectedGovBalizaId?.let { govId ->
                val govBaliza = uiState.govBalizas.find { it.id == govId }
                if (govBaliza != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight()
                            .background(CardBackground)
                            .align(Alignment.CenterStart)
                    ) {
                        GovBalizaPanel(govBaliza = govBaliza, onDismiss = viewModel::dismissGovPanel)
                    }
                }
            }

            // ── Dialogs ───────────────────────────────────────────────────
            if (uiState.isAddressSearchOpen) {
                AddressSearchDialog(
                    query = uiState.addressQuery,
                    suggestions = uiState.addressSuggestions,
                    isSearching = uiState.isSearchingAddress,
                    error = uiState.addressSearchError,
                    onQueryChange = viewModel::onAddressQueryChange,
                    onSearch = viewModel::searchAddress,
                    onSuggestionSelected = { suggestion ->
                        viewModel.placeEmptyBaliza(suggestion.lat, suggestion.lon)
                    },
                    onDismiss = { viewModel.toggleAddressSearch(false) }
                )
            }

            if (uiState.isProposalFormOpen) {
                ProposalFormDialog(
                    onSend = { t, d, s, e, c, p -> viewModel.sendProposal(t, d, s, e, c, p) },
                    onDismiss = { viewModel.toggleProposalForm(false) }
                )
            }


            if (uiState.isLoading) {
                CircularProgressIndicator(color = InteresadoGradientStart, modifier = Modifier.align(Alignment.Center))
            }

            // ── Filter Bottom Sheet ───────────────────────────────────────
            if (uiState.isFilterSheetVisible) {
                FilterBottomSheet(
                    uiState = uiState,
                    onDismiss = { viewModel.toggleFilterSheet(false) },
                    onToggleTag = viewModel::onToggleTag,
                    onPriceChange = viewModel::onPriceChange,
                    onDateRangeChange = viewModel::onDateRangeChange
                )
            }

            // ── Google Maps Redirect Dialog ───────────────────────────────
            if (showGoogleMapsDialog && pendingGoogleMapsPin != null) {
                val pin = pendingGoogleMapsPin!!
                AlertDialog(
                    onDismissRequest = { showGoogleMapsDialog = false },
                    title = { Text(text = "Ver en Google Maps", color = Color.White) },
                    text  = { Text(text = "¿Deseas abrir la ubicación de \"${pin.titulo}\" en la aplicación de Google Maps?", color = Color.LightGray) },
                    confirmButton = {
                        TextButton(onClick = {
                            showGoogleMapsDialog = false
                            val gmmIntentUri = android.net.Uri.parse("geo:${pin.lat},${pin.lon}?q=${android.net.Uri.encode(pin.titulo + ", " + pin.galeria)}")
                            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try { context.startActivity(mapIntent) } catch (_: Exception) {
                                try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)) } catch (_: Exception) { }
                            }
                        }) { Text("Sí", color = ArtistaGradientStart) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGoogleMapsDialog = false }) { Text("No", color = TextGray) }
                    },
                    containerColor = CardBackground,
                    textContentColor = Color.LightGray,
                    titleContentColor = Color.White
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// Search Bar
// --------------------------------------------------------------------------
@Composable
fun FunctionalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    var tfv by remember { mutableStateOf(TextFieldValue(query)) }
    LaunchedEffect(query) { if (query != tfv.text) tfv = TextFieldValue(query) }

    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = CardBackground.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (tfv.text.isEmpty()) Text("Galerías, obras o artistas", color = TextGray, fontSize = 15.sp)
                BasicTextField(
                    value = tfv,
                    onValueChange = { tfv = it; onQueryChange(it.text) },
                    textStyle = TextStyle(color = TextWhite, fontSize = 15.sp),
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
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd)))
                    .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// --------------------------------------------------------------------------
// Filter Bottom Sheet
// --------------------------------------------------------------------------
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
        initialSelectedEndDateMillis   = uiState.endDate
    )
    LaunchedEffect(uiState.startDate, uiState.endDate) {
        if (uiState.startDate != datePickerState.selectedStartDateMillis ||
            uiState.endDate   != datePickerState.selectedEndDateMillis) {
            datePickerState.setSelection(uiState.startDate, uiState.endDate)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateRangeChange(datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis)
                    showDatePicker = false
                }) { Text("OK", color = InteresadoGradientStart) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextGray) }
            },
            colors = DatePickerDefaults.colors(containerColor = CardBackground)
        ) {
            DateRangePicker(
                state = datePickerState,
                title = { Text("Selecciona el periodo", modifier = Modifier.padding(start = 24.dp, top = 16.dp), color = TextGray, fontSize = 14.sp) },
                headline = {
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                        val startText = datePickerState.selectedStartDateMillis?.let { sdf.format(Date(it)) } ?: "Inicio"
                        val endText   = datePickerState.selectedEndDateMillis?.let { sdf.format(Date(it)) } ?: "Fin"
                        Text("$startText — $endText", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = CardBackground, titleContentColor = TextWhite, headlineContentColor = TextWhite,
                    weekdayContentColor = TextGray, dayContentColor = TextWhite,
                    selectedDayContainerColor = InteresadoGradientStart, selectedDayContentColor = TextWhite,
                    todayContentColor = InteresadoGradientStart, todayDateBorderColor = InteresadoGradientStart,
                    dayInSelectionRangeContainerColor = InteresadoGradientStart.copy(alpha = 0.2f),
                    dayInSelectionRangeContentColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextGray.copy(alpha = 0.5f)) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text("Filtros", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

            FilterSectionTitle("Disciplina")
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pintura", "Escultura", "Fotografía").forEach { tag ->
                    FilterChipItem(text = tag, isSelected = uiState.selectedTags.contains(tag), onClick = { onToggleTag(tag) })
                }
            }

            FilterSectionTitle("Precio Máximo: ${if (uiState.maxPrice >= 100f) "Cualquiera" else "${uiState.maxPrice.toInt()}€"}")
            Slider(
                value = uiState.maxPrice, onValueChange = onPriceChange, valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = InteresadoGradientStart, activeTrackColor = InteresadoGradientStart, inactiveTrackColor = TextGray.copy(alpha = 0.2f)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            FilterSectionTitle("Rango de Fechas")
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).clickable { showDatePicker = true },
                color = CardBackground.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = InteresadoGradientStart)
                    Spacer(modifier = Modifier.width(12.dp))
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val dateText = when {
                        uiState.startDate != null && uiState.endDate != null -> "${sdf.format(Date(uiState.startDate))} - ${sdf.format(Date(uiState.endDate))}"
                        uiState.startDate != null -> "Desde ${sdf.format(Date(uiState.startDate))}"
                        else -> "Seleccionar rango"
                    }
                    Text(dateText, color = if (uiState.startDate != null) TextWhite else TextGray, fontSize = 15.sp, modifier = Modifier.weight(1f))
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aplicar Filtros", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// Utility composables
// --------------------------------------------------------------------------
@Composable
fun FilterSectionTitle(title: String) {
    Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color.Transparent else CardBackground.copy(alpha = 0.8f),
        border = if (isSelected) null else BorderStroke(1.dp, TextGray.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.then(
                if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(InteresadoGradientStart, InteresadoGradientEnd)))
                else Modifier
            ).padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = if (isSelected) TextWhite else TextGray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// --------------------------------------------------------------------------
// Map info cards
// --------------------------------------------------------------------------
@Composable
fun ExhibitionMapCard(pin: MapPin, isClickable: Boolean = true, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth().then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.95f),
        tonalElevation = 12.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = pin.imagen, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pin.titulo, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(pin.galeria, color = TextGray, fontSize = 13.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("${pin.match}% match", color = InteresadoGradientStart, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(" · ${pin.distancia}", color = TextGray, fontSize = 12.sp)
                }
            }
            val (icon, gradient) = when (pin.mainTag.lowercase()) {
                "fotografía" -> Icons.Default.Image to listOf(InteresadoGradientStart, InteresadoGradientEnd)
                "escultura"  -> Icons.Default.AccountBalance to listOf(ArtistaGradientStart, ArtistaGradientEnd)
                else         -> Icons.Default.Palette to listOf(InteresadoGradientStart, InteresadoGradientEnd)
            }
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Brush.horizontalGradient(gradient)),
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
    isOwner: Boolean,
    onCreateExhibition: () -> Unit,
    onSendProposal: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = CardBackground.copy(alpha = 0.97f), tonalElevation = 12.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF374151)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isOwner) "Tu baliza vacía" else "Baliza disponible", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (isLoadingAddress) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(10.dp), color = TextGray, strokeWidth = 1.5.dp)
                                Text("Obteniendo dirección...", color = TextGray, fontSize = 11.sp)
                            }
                        } else {
                            Text(address ?: "${"%.5f".format(baliza.lat)}, ${"%.5f".format(baliza.lon)}", color = TextGray, fontSize = 11.sp, maxLines = 2)
                        }
                    }
                }
                Row {
                    if (isOwner) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close,  contentDescription = "Cerrar",   tint = TextGray,           modifier = Modifier.size(18.dp)) }
                }
            }
            if (isOwner) {
                Button(onClick = onCreateExhibition, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))), contentAlignment = Alignment.Center) {
                        Text("Crear exposición aquí", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                Button(onClick = onSendProposal, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))), contentAlignment = Alignment.Center) {
                        Text("Proponer exposición", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InteresadoEmptyBalizaCard(address: String?, isLoadingAddress: Boolean, isOwner: Boolean, onDelete: () -> Unit, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = CardBackground.copy(alpha = 0.97f), tonalElevation = 12.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF374151)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isOwner) "Tu baliza vacía" else "Baliza vacía", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (isLoadingAddress) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(10.dp), color = TextGray, strokeWidth = 1.5.dp)
                        Text("Obteniendo dirección...", color = TextGray, fontSize = 13.sp)
                    }
                } else {
                    Text(address ?: "Sin exposición asignada todavía", color = TextGray, fontSize = 13.sp, maxLines = 2)
                }
            }
            if (isOwner) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close,  contentDescription = "Cerrar",   tint = TextGray,           modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun AddressSearchDialog(
    query: String,
    suggestions: List<AddressSuggestion>,
    isSearching: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Buscar dirección", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ej: Calle Larios, Málaga") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite, unfocusedTextColor = TextGray,
                        focusedLabelColor = ArtistaGradientStart, cursorColor = ArtistaGradientStart,
                        focusedBorderColor = ArtistaGradientStart, unfocusedBorderColor = TextGray
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSearch(query) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = query.isNotBlank() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Buscar")
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color.Red, fontSize = 14.sp)
                }

                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Resultados:", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(min = 100.dp, max = 250.dp)) {
                        items(suggestions) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSuggestionSelected(suggestion) }
                                    .padding(vertical = 12.dp), // slightly more padding for touch targets
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(suggestion.displayName, color = TextWhite, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                            Divider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProposalFormDialog(
    onSend: (titulo: String, descrip: String, start: String, end: String, category: String, price: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descrip by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Proponer Exposición", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it },
                    label = { Text("Título de la exposición") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoria, onValueChange = { categoria = it },
                    label = { Text("Categoría (e.g., Pintura)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descrip, onValueChange = { descrip = it },
                    label = { Text("Descripción breve") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fechaInicio, onValueChange = { fechaInicio = it },
                        label = { Text("Inicio (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                    OutlinedTextField(
                        value = fechaFin, onValueChange = { fechaFin = it },
                        label = { Text("Fin (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextGray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSend(titulo, descrip, fechaInicio, fechaFin, categoria, precioStr.toDoubleOrNull()) }) {
                        Text("Enviar Propuesta")
                    }
                }
            }
        }
    }
}

@Composable
fun GovBalizaPanel(
    govBaliza: com.antigravity.swart.domain.model.GovBaliza,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Espacio Gubernamental", color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, tint = TextWhite, contentDescription = "Cerrar") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(govBaliza.nombre, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(govBaliza.direccion, color = TextGray, fontSize = 14.sp)
        }
        if (govBaliza.telefono != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(govBaliza.telefono, color = TextGray, fontSize = 14.sp)
            }
        }
        if (govBaliza.email != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(govBaliza.email, color = TextGray, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Nota: Este espacio pertenece a la administración pública. No puedes reservar exposiciones a través de Swart para este lugar. Contacta directamente para más información.", color = Color(0xFF9CA3AF), fontSize = 12.sp, lineHeight = 18.sp)
    }
}
