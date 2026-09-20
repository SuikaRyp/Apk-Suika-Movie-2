package com.suikamovie.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain
import kotlin.math.absoluteValue

/**
 * Layar penuh dengan gesture "swipe up to continue" - dipakai buat layar
 * Welcome & Disclaimer. Port dari setupSwipeUpToContinue() di auth.js, versi
 * Compose: drag vertikal ke atas minimal 60dp dianggap swipe, atau bisa juga
 * di-tap langsung tombolnya sebagai fallback.
 */
@Composable
fun SwipeUpScreen(
    brandName: String,
    appVersion: String,
    onContinue: () -> Unit,
    content: @Composable Column.() -> Unit,
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBody)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                        // Geser ke atas = dragAmount negatif. Total geser ke
                        // atas minimal ~60px (density-independent enough
                        // buat kebanyakan device) dianggap "swipe up".
                        if (accumulatedDrag < -180f) {
                            onContinue()
                        }
                    },
                    onDragEnd = { accumulatedDrag = 0f },
                )
            }
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                buildString { append(brandName) },
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
            )
            Text(
                "App: $appVersion",
                color = TextLight,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            content()
        }

        SwipeUpIndicator(onTap = onContinue)
    }
}

@Composable
private fun SwipeUpIndicator(onTap: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val transition = rememberInfiniteTransition(label = "swipe-up-dot")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "swipe-up-dot-progress",
        )

        Box(
            modifier = Modifier
                .width(26.dp)
                .height(66.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                .clickable(onClick = onTap),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val dotAlpha = if (progress < 0.65f) 1f - (progress / 0.65f) else 0f
            val dotOffsetY = -(progress.coerceAtMost(0.65f) / 0.65f) * 38
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .offset(y = dotOffsetY.dp)
                    .size(8.dp)
                    .background(Color.White.copy(alpha = dotAlpha.absoluteValue.coerceIn(0f, 1f)), CircleShape)
            )
        }

        Text(
            "Swipe Up to continue",
            color = TextLight,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
        )
    }
}
