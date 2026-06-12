package com.antigravity.swart.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
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
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


// --------------------------------------------------------------------------
// In-memory bitmap URL cache to avoid repeated network calls
// --------------------------------------------------------------------------
private val bitmapUrlCache = java.util.concurrent.ConcurrentHashMap<String, Bitmap?>()

fun loadBitmapFromUrl(url: String): Bitmap? {
    if (bitmapUrlCache.containsKey(url)) return bitmapUrlCache[url]
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        val raw = BitmapFactory.decodeStream(input)
        bitmapUrlCache[url] = raw
        raw
    } catch (_: Exception) {
        bitmapUrlCache[url] = null
        null
    }
}

fun cropSquare(bmp: Bitmap): Bitmap {
    val side = minOf(bmp.width, bmp.height)
    val x = (bmp.width - side) / 2
    val y = (bmp.height - side) / 2
    return Bitmap.createBitmap(bmp, x, y, side, side)
}

fun roundedBitmap(bmp: Bitmap, size: Int, cornerRadius: Float): Bitmap {
    val scaled = Bitmap.createScaledBitmap(bmp, size, size, true)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(scaled, 0f, 0f, paint)
    return output
}

// --------------------------------------------------------------------------
// Main stacked-cards marker bitmap
// --------------------------------------------------------------------------
/**
 * Draws a stacked 3-card marker:
 *  - Back card  (pink/beige tint)  : offset left+up
 *  - Middle card (grey tint)       : offset left+up slightly
 *  - Front card (full color, img1) : top-right, sharp
 *  - Pill badge "+N" bottom-right of front card
 *  - Category icon centered over all 3 cards
 */
fun createStackedMarkerBitmap(
    context: Context,
    tag: String,
    img1: Bitmap?,
    img2: Bitmap?,
    img3: Bitmap?,
    extraObras: Int   // obras beyond the 3 shown  (totalObras - 3, clamped >= 0)
): Bitmap {
    // ── Dimensions ──────────────────────────────────────────────────────────
    val cardSize   = 160          // side of each square card (px)
    val radius     = 32f          // corner radius
    val offset     = 22           // horizontal/vertical offset between cards
    val totalW     = cardSize + offset * 2 + 20   // extra right margin for shadow
    val totalH     = cardSize + offset * 2 + 20
    val bitmap     = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
    val canvas     = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Helper: draw a tinted rounded card ──────────────────────────────────
    fun drawCard(left: Float, top: Float, photoBmp: Bitmap?, tintColor: Int) {
        val rect = RectF(left, top, left + cardSize, top + cardSize)
        // shadow
        paint.color = 0x40000000; paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect.apply { offset(4f, 4f) }, radius, radius, paint)
        rect.offset(-4f, -4f)
        // card base
        paint.color = if (photoBmp == null) 0xFF2D2D3A.toInt() else 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)

        if (photoBmp != null) {
            // clip photo to rounded rect
            val saveCount = canvas.saveLayer(rect, null)
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val scaled = Bitmap.createScaledBitmap(photoBmp, cardSize, cardSize, true)
            canvas.drawBitmap(scaled, left, top, paint)
            paint.xfermode = null
            canvas.restoreToCount(saveCount)
        }

        // tint overlay
        if (tintColor != 0) {
            paint.color = tintColor
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }

    // ── Back card: pink/beige tint, furthest left ────────────────────────────
    val back3x = 0f
    val back3y = offset * 2f
    drawCard(back3x, back3y, img3, 0xAAF3E8D8.toInt())

    // ── Middle card: grey tint, slightly left ────────────────────────────────
    val mid2x = offset.toFloat()
    val mid2y = offset.toFloat()
    drawCard(mid2x, mid2y, img2, 0x88B0B0B0.toInt())

    // ── Front card: no tint (full photo) ────────────────────────────────────
    val frontX = (offset * 2).toFloat()
    val frontY = 0f
    drawCard(frontX, frontY, img1, 0)

    // ── Pill badge "+N" ──────────────────────────────────────────────────────
    if (extraObras > 0) {
        val badgeText  = "+$extraObras"
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        badgePaint.textSize   = 22f
        badgePaint.typeface   = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        badgePaint.color      = 0xFF222222.toInt()
        val textW  = badgePaint.measureText(badgeText)
        val padH   = 10f; val padV = 6f
        val bW     = textW + padH * 2
        val bH     = 28f
        val bLeft  = frontX + cardSize - bW - 10f
        val bTop   = frontY + cardSize - bH - 10f
        val bRect  = RectF(bLeft, bTop, bLeft + bW, bTop + bH)
        // white background
        badgePaint.color = 0xFFFFFFFF.toInt()
        canvas.drawRoundRect(bRect, bH / 2f, bH / 2f, badgePaint)
        // text
        badgePaint.color = 0xFF111111.toInt()
        val fontMetrics = badgePaint.fontMetrics
        val textY = bTop + bH / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(badgeText, bLeft + padH, textY, badgePaint)
    }

    // ── Category icon centered over all 3 cards ──────────────────────────────
    val iconSize   = 44
    val iconRadius = iconSize / 2f
    // geometric center of the three cards bounding box
    val allLeft    = back3x; val allRight = frontX + cardSize
    val allTop     = frontY; val allBottom = back3y + cardSize
    val iconCx     = (allLeft + allRight) / 2f
    val iconCy     = (allTop + allBottom) / 2f

    // gradient pill bg
    val (gradStart, gradEnd) = when (tag.lowercase()) {
        "escultura"  -> 0xFFEC4899.toInt() to 0xFF8B5CF6.toInt()
        "fotografía" -> 0xFF6366F1.toInt() to 0xFF3B82F6.toInt()
        else         -> 0xFF6366F1.toInt() to 0xFF3B82F6.toInt()
    }
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // shadow
    iconPaint.color = 0x55000000
    canvas.drawCircle(iconCx + 2f, iconCy + 2f, iconRadius + 2f, iconPaint)
    // gradient circle
    iconPaint.shader = LinearGradient(
        iconCx - iconRadius, iconCy - iconRadius,
        iconCx + iconRadius, iconCy + iconRadius,
        gradStart, gradEnd, Shader.TileMode.CLAMP
    )
    canvas.drawCircle(iconCx, iconCy, iconRadius.toFloat(), iconPaint)
    iconPaint.shader = null

    // draw icon path (white)
    iconPaint.color = 0xFFFFFFFF.toInt()
    iconPaint.style = Paint.Style.FILL
    val s  = iconSize / 22f
    val cx = iconCx; val cy = iconCy; val off = 11f
    val mainPath = Path()
    when (tag.lowercase()) {
        "fotografía" -> {
            // camera outline
            mainPath.moveTo(cx+(21f-off)*s, cy+(19f-off)*s); mainPath.lineTo(cx+(21f-off)*s, cy+(5f-off)*s)
            mainPath.cubicTo(cx+(21f-off)*s, cy+(3.9f-off)*s, cx+(20.1f-off)*s, cy+(3f-off)*s, cx+(19f-off)*s, cy+(3f-off)*s)
            mainPath.lineTo(cx+(5f-off)*s, cy+(3f-off)*s)
            mainPath.cubicTo(cx+(3.9f-off)*s, cy+(3f-off)*s, cx+(3f-off)*s, cy+(3.9f-off)*s, cx+(3f-off)*s, cy+(5f-off)*s)
            mainPath.lineTo(cx+(3f-off)*s, cy+(19f-off)*s)
            mainPath.cubicTo(cx+(3f-off)*s, cy+(20.1f-off)*s, cx+(3.9f-off)*s, cy+(21f-off)*s, cx+(5f-off)*s, cy+(21f-off)*s)
            mainPath.lineTo(cx+(19f-off)*s, cy+(21f-off)*s)
            mainPath.cubicTo(cx+(20.1f-off)*s, cy+(21f-off)*s, cx+(21f-off)*s, cy+(20.1f-off)*s, cx+(21f-off)*s, cy+(19f-off)*s)
            mainPath.close()
        }
        "escultura" -> {
            mainPath.addRect(cx+(2f-off)*s, cy+(19f-off)*s, cx+(21f-off)*s, cy+(22f-off)*s, Path.Direction.CW)
            mainPath.addRect(cx+(4f-off)*s, cy+(10f-off)*s, cx+(7f-off)*s, cy+(17f-off)*s, Path.Direction.CW)
            mainPath.addRect(cx+(10f-off)*s, cy+(10f-off)*s, cx+(13f-off)*s, cy+(17f-off)*s, Path.Direction.CW)
            mainPath.addRect(cx+(16f-off)*s, cy+(10f-off)*s, cx+(19f-off)*s, cy+(17f-off)*s, Path.Direction.CW)
            mainPath.moveTo(cx+(11.5f-off)*s, cy+(2f-off)*s); mainPath.lineTo(cx+(2f-off)*s, cy+(6f-off)*s)
            mainPath.lineTo(cx+(2f-off)*s, cy+(8f-off)*s); mainPath.lineTo(cx+(21f-off)*s, cy+(8f-off)*s)
            mainPath.lineTo(cx+(21f-off)*s, cy+(6f-off)*s); mainPath.close()
        }
        else -> {
            // palette
            mainPath.moveTo(cx+(12f-off)*s, cy+(2f-off)*s)
            mainPath.cubicTo(cx+(6.48f-off)*s, cy+(2f-off)*s, cx+(2f-off)*s, cy+(6.48f-off)*s, cx+(2f-off)*s, cy+(12f-off)*s)
            mainPath.cubicTo(cx+(2f-off)*s, cy+(17.52f-off)*s, cx+(6.48f-off)*s, cy+(22f-off)*s, cx+(12f-off)*s, cy+(22f-off)*s)
            mainPath.cubicTo(cx+(12.83f-off)*s, cy+(22f-off)*s, cx+(13.5f-off)*s, cy+(21.33f-off)*s, cx+(13.5f-off)*s, cy+(20.5f-off)*s)
            mainPath.cubicTo(cx+(13.5f-off)*s, cy+(20.11f-off)*s, cx+(13.35f-off)*s, cy+(19.76f-off)*s, cx+(13.11f-off)*s, cy+(19.49f-off)*s)
            mainPath.cubicTo(cx+(12.88f-off)*s, cy+(19.23f-off)*s, cx+(12.73f-off)*s, cy+(18.88f-off)*s, cx+(12.73f-off)*s, cy+(18.5f-off)*s)
            mainPath.cubicTo(cx+(12.73f-off)*s, cy+(17.67f-off)*s, cx+(13.4f-off)*s, cy+(17f-off)*s, cx+(14.23f-off)*s, cy+(17f-off)*s)
            mainPath.lineTo(cx+(16f-off)*s, cy+(17f-off)*s)
            mainPath.cubicTo(cx+(18.76f-off)*s, cy+(17f-off)*s, cx+(21f-off)*s, cy+(14.76f-off)*s, cx+(21f-off)*s, cy+(12f-off)*s)
            mainPath.cubicTo(cx+(21f-off)*s, cy+(6.48f-off)*s, cx+(16.52f-off)*s, cy+(2f-off)*s, cx+(12f-off)*s, cy+(2f-off)*s)
            mainPath.close()
            // palette holes  (drawn in gradient color)
            val holePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            holePaint.shader = LinearGradient(
                iconCx - iconRadius, iconCy - iconRadius,
                iconCx + iconRadius, iconCy + iconRadius,
                gradStart, gradEnd, Shader.TileMode.CLAMP
            )
            canvas.drawPath(mainPath, iconPaint)
            canvas.drawCircle(cx+(6.5f-off)*s,  cy+(10.5f-off)*s, 1.8f*s, holePaint)
            canvas.drawCircle(cx+(9.5f-off)*s,  cy+(6.5f-off)*s,  1.8f*s, holePaint)
            canvas.drawCircle(cx+(14.5f-off)*s, cy+(6.5f-off)*s,  1.8f*s, holePaint)
            canvas.drawCircle(cx+(17.5f-off)*s, cy+(10.5f-off)*s, 1.8f*s, holePaint)
            return bitmap
        }
    }
    canvas.drawPath(mainPath, iconPaint)

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


                    // ── Register exhibition bitmaps in style ──────────────
                    uiState.filteredPins.forEach { pin ->
                        val imgId = "marker-${pin.idExposicion}"
                        if (style.getStyleImage(imgId) == null) {
                            // Load images asynchronously (network); update style on main thread
                            Thread {
                                val img1 = pin.imagen?.let { loadBitmapFromUrl(it) }
                                val img2 = pin.imagen2?.let { loadBitmapFromUrl(it) }
                                val img3 = pin.imagen3?.let { loadBitmapFromUrl(it) }
                                val extraObras = (pin.totalObras - 3).coerceAtLeast(0)
                                val bmp = createStackedMarkerBitmap(
                                    context, pin.mainTag, img1, img2, img3, extraObras
                                )
                                mv.post {
                                    if (style.getStyleImage(imgId) == null) {
                                        style.addImage(imgId, bmp)
                                    }
                                }
                            }.start()
                        }
                    }


                    // Register empty-baliza bitmap
                    val balizaBmp = createEmptyBalizaMarker(context)
                    if (style.getStyleImage("marker-baliza") == null) {
                        style.addImage("marker-baliza", balizaBmp)
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

                    // ── Exhibition symbol layer ───────────────────────────
                    val exhibLayer = "layer-exhibitions"
                    if (!style.styleLayerExists(exhibLayer)) {
                        style.addLayer(
                            symbolLayer(exhibLayer, exhibSource) {
                                iconImage(get("markerImage"))
                                iconAnchor(IconAnchor.CENTER)
                                iconAllowOverlap(true)
                                iconIgnorePlacement(true)
                            }
                        )
                        // Click handler
                        mv.gestures.addOnMapClickListener { clickPoint ->
                            val screenPoint = mapboxMap.pixelForCoordinate(clickPoint)
                            mapboxMap.queryRenderedFeatures(
                                com.mapbox.maps.RenderedQueryGeometry(com.mapbox.maps.ScreenCoordinate(screenPoint.x, screenPoint.y)),
                                com.mapbox.maps.RenderedQueryOptions(
                                    listOf(exhibLayer, "layer-balizas"), null
                                )
                            ) { result ->
                                result.value?.firstOrNull()?.queriedFeature?.feature?.let { feature ->
                                    val pinIdStr    = feature.getStringProperty("pinId")
                                    val balizaIdStr = feature.getStringProperty("balizaId")
                                    if (pinIdStr != null) {
                                        uiState.filteredPins.find { it.idExposicion.toString() == pinIdStr }
                                            ?.let { viewModel.onPinClick(it) }
                                    } else if (balizaIdStr != null) {
                                        uiState.emptyBalizas.find { it.id.toString() == balizaIdStr }
                                            ?.let { viewModel.onEmptyBalizaClick(it) }
                                    }
                                    Unit
                                }
                            }
                            false
                        }
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
                if (!uiState.isPlacingMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FloatingActionButton(
                        onClick = { viewModel.togglePlacingMode() },
                        containerColor = Color(0xFF374151),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddLocation, contentDescription = "Colocar baliza vacía")
                    }
                }
            }

            // ── Selected Exhibition Card ──────────────────────────────────
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

                // ── Empty Baliza Card ─────────────────────────────────────
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
                                onDelete  = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        } else {
                            InteresadoEmptyBalizaCard(
                                address = uiState.selectedEmptyBalizaAddress,
                                isLoadingAddress = uiState.isLoadingBalizaAddress,
                                onDelete  = { viewModel.deleteEmptyBaliza(baliza.id) },
                                onDismiss = { viewModel.dismissEmptyBaliza() }
                            )
                        }
                    }
                }
            }

            // ── Placing Mode Overlay ──────────────────────────────────────
            if (uiState.isPlacingMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 152.dp, start = 16.dp, end = 16.dp)
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
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                            Text("Mueve el mapa para posicionar la baliza", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Box(modifier = Modifier.size(48.dp).align(Alignment.Center)) {
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.9f)).align(Alignment.Center))
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.9f)).align(Alignment.Center))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6B7280)).align(Alignment.Center))
                }

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
                        border = BorderStroke(1.dp, Color(0xFF6B7280))
                    ) {
                        Text("Cancelar", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val center = mapViewRef?.mapboxMap?.cameraState?.center
                            if (center != null) viewModel.placeEmptyBaliza(center.latitude(), center.longitude())
                        },
                        enabled = !uiState.isCreatingEmptyBaliza,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))
                    ) {
                        if (uiState.isCreatingEmptyBaliza) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Colocar aquí", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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
    onCreateExhibition: () -> Unit,
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
                        Text("Baliza vacía", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                    IconButton(onClick = onDelete,  modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close,  contentDescription = "Cerrar",   tint = TextGray,           modifier = Modifier.size(18.dp)) }
                }
            }
            Button(onClick = onCreateExhibition, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(ArtistaGradientStart, ArtistaGradientEnd))), contentAlignment = Alignment.Center) {
                    Text("Crear exposición aquí", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun InteresadoEmptyBalizaCard(address: String?, isLoadingAddress: Boolean, onDelete: () -> Unit, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = CardBackground.copy(alpha = 0.97f), tonalElevation = 12.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF374151)), contentAlignment = Alignment.Center) {
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
                    Text(address ?: "Sin exposición asignada todavía", color = TextGray, fontSize = 13.sp, maxLines = 2)
                }
            }
            IconButton(onClick = onDelete,  modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close,  contentDescription = "Cerrar",   tint = TextGray,           modifier = Modifier.size(18.dp)) }
        }
    }
}
