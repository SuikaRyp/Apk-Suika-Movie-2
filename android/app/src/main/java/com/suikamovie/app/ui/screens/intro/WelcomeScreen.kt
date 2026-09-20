package com.suikamovie.app.ui.screens.intro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.suikamovie.app.ui.components.SwipeUpScreen
import com.suikamovie.app.ui.theme.TextMain

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    SwipeUpScreen(
        brandName = "SUIKAMOVIE",
        appVersion = "2.0 (2)",
        onContinue = onContinue,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                ".WELCOME.",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
            )
        }
    }
}
