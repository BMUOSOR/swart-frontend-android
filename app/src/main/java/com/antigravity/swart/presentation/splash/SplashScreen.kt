package com.antigravity.swart.presentation.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*
import com.antigravity.swart.R
import com.antigravity.swart.presentation.theme.DarkBackground

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var isPlaying by remember { mutableStateOf(true) }
    var alpha by remember { mutableStateOf(1f) }

    // Fade out animation that triggers when alpha state changes to 0f
    val alphaAnim by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            onAnimationFinished()
        },
        label = "SplashFadeOut"
    )

    // Load the Lottie composition from res/raw
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.liquid_transition_screen))
    
    // Animate the Lottie file
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
    )

    // When the Lottie animation reaches the end (1f), stop playing and start fading out
    LaunchedEffect(progress) {
        if (progress == 1f && isPlaying) {
            isPlaying = false
            alpha = 0f // This triggers the alphaAnim to fade out over 800ms
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
