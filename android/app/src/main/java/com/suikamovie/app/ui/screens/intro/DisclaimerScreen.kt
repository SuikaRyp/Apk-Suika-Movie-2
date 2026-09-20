package com.suikamovie.app.ui.screens.intro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suikamovie.app.ui.components.SwipeUpScreen
import com.suikamovie.app.ui.theme.TextMain
import com.suikamovie.app.ui.theme.TextSub

@Composable
fun DisclaimerScreen(onContinue: () -> Unit) {
    SwipeUpScreen(
        brandName = "SUIKAMOVIE",
        appVersion = "1.0 (1)",
        onContinue = onContinue,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Disclaimer",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                modifier = Modifier.padding(bottom = 18.dp),
            )
            Text(
                "SuikaMovie adalah aplikasi tidak resmi (unofficial). Seluruh merek dagang " +
                    "dan hak cipta adalah milik pemegang haknya masing-masing. Kami tidak " +
                    "bertanggung jawab atas konten yang di-hosting oleh pihak ketiga dan " +
                    "tidak terlibat dalam proses upload/download konten tersebut — kami " +
                    "cuma menyediakan tautan yang tersedia di internet.",
                color = TextSub,
                fontSize = 13.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            Text(
                buildString {
                    append("Kalau menurut kamu ada konten yang melanggar hak kekayaan intelektual ")
                    append("dan kamu adalah pemegang hak cipta konten tersebut, silakan laporkan ")
                    append("ke admin@suikamovie.app dan konten tersebut akan langsung dihapus. ")
                    append("Dengan menggunakan aplikasi ini, kamu dianggap sudah menyetujui kebijakan ini.")
                },
                color = TextSub,
                fontSize = 13.sp,
                lineHeight = 21.sp,
            )
        }
    }
}
