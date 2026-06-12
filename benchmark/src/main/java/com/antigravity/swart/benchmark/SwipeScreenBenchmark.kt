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
class SwipeScreenBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkCardSwiping() = benchmarkRule.measureRepeated(
        packageName = "com.antigravity.swart",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.None(),
        iterations = 5,
        setupBlock = {
            pressHome()
            
            // Lanzamiento manual de la app para evitar el bug "Unable to confirm activity launch completion" de algunos dispositivos
            val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            val intent = context.packageManager.getLaunchIntentForPackage("com.antigravity.swart")!!
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            
            // Esperar a que la app esté en primer plano
            device.wait(Until.hasObject(By.pkg("com.antigravity.swart")), 10000)
            device.waitForIdle()
            
            // Iniciar sesión si estamos en la pantalla de Login
            val usuarioField = device.wait(Until.findObject(By.text("Usuario")), 4000)
            if (usuarioField != null) {
                usuarioField.text = "chica_3"
                val passwordField = device.findObject(By.text("Contraseña"))
                passwordField.text = "1"
                val loginBtn = device.findObject(By.text("Iniciar sesión"))
                loginBtn.click()
                device.wait(Until.gone(By.text("Iniciar sesión")), 10000)
            }
            
            // Navegar a la pestaña de descubrimiento (Swipe)
            val discoverTab = device.wait(Until.findObject(By.desc("DiscoverTab")), 8000)
            discoverTab?.click()
            device.waitForIdle()
        }
    ) {
        // Encontrar la tarjeta de Swipe actual
        val swipeCard = device.findObject(By.desc("SwipeCard"))
        
        if (swipeCard != null) {
            // Realizar swipes hacia la derecha
            repeat(5) {
                swipeCard.swipe(Direction.RIGHT, 0.8f, 500)
                device.waitForIdle()
            }
        }
    }
}
