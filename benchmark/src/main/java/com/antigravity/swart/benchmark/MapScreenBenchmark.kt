package com.antigravity.swart.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MapScreenBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkMapNavigationAndFilter() = benchmarkRule.measureRepeated(
        packageName = "com.antigravity.swart",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.None(),
        iterations = 5,
        setupBlock = {
            pressHome()

            val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            val intent = context.packageManager.getLaunchIntentForPackage("com.antigravity.swart")!!
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)

            device.wait(Until.hasObject(By.pkg("com.antigravity.swart")), 10000)
            device.waitForIdle()

            val usuarioField = device.wait(Until.findObject(By.text("Usuario")), 4000)
            if (usuarioField != null) {
                usuarioField.text = "chica_3"
                val passwordField = device.findObject(By.text("Contraseña"))
                passwordField.text = "1"
                val loginBtn = device.findObject(By.text("Iniciar sesión"))
                loginBtn.click()
                device.wait(Until.gone(By.text("Iniciar sesión")), 10000)
            }

            val mapTab = device.wait(Until.findObject(By.desc("MapTab")), 8000)
            mapTab?.click()
            device.waitForIdle()
        }
    ) {
        val mapView = device.findObject(By.desc("GoogleMapView"))
        
        if (mapView != null) {
            mapView.swipe(Direction.DOWN, 0.5f, 1000)
            device.waitForIdle()
            mapView.swipe(Direction.RIGHT, 0.5f, 1000)
            device.waitForIdle()
        }

        val searchField = device.findObject(By.desc("MapSearchField"))
        if (searchField != null) {
            searchField.text = "Galería"
            device.waitForIdle()
        }
    }
}
