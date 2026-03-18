package com.farmlab.labfarmis.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmlab.labfarmis.ui.theme.FarmGreen
import com.farmlab.labfarmis.ui.theme.FarmGreenDark
import com.farmlab.labfarmis.ui.theme.FarmGreenLight
import com.farmlab.labfarmis.ui.theme.FarmYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val dotAlpha1 = remember { Animatable(0.3f) }
    val dotAlpha2 = remember { Animatable(0.3f) }
    val dotAlpha3 = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f))
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(200)
        subtitleAlpha.animateTo(1f, animationSpec = tween(500))
        delay(400)
        // Pulsing loading dots
        repeat(4) {
            dotAlpha1.animateTo(1f, animationSpec = tween(180)); dotAlpha1.animateTo(0.3f, animationSpec = tween(180))
            dotAlpha2.animateTo(1f, animationSpec = tween(180)); dotAlpha2.animateTo(0.3f, animationSpec = tween(180))
            dotAlpha3.animateTo(1f, animationSpec = tween(180)); dotAlpha3.animateTo(0.3f, animationSpec = tween(180))
        }
        delay(300)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FarmGreenDark, FarmGreen, FarmGreenLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App name
            Column(
                modifier = Modifier.alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "FarmLab",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(FarmYellow)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(FarmYellow)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(FarmYellow)
                    )
                }
            }

            // Tagline
            Text(
                text = "Smart Farm in Your Pocket",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.alpha(subtitleAlpha.value),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .alpha(dotAlpha1.value)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .alpha(dotAlpha2.value)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .alpha(dotAlpha3.value)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
