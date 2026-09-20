package com.suikamovie.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.BgCard
import com.suikamovie.app.ui.theme.BgSurface
import com.suikamovie.app.ui.theme.DangerRed
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain
import com.suikamovie.app.ui.theme.TextSub

@Composable
fun AccountScreen(onSignedOut: () -> Unit) {
    val viewModel: AccountViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBody)
            .padding(16.dp),
    ) {
        Text("Akun", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Kartu profil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!profile?.photoURL.isNullOrBlank()) {
                AsyncImage(
                    model = profile?.photoURL,
                    contentDescription = "Foto profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(BgSurface).padding(10.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(profile?.displayName ?: "Pengguna", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(profile?.email ?: "", color = TextSub, fontSize = 12.sp)
                if (uiState.isAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                        Text(" OWNER", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Progress Level & EXP
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .background(BgCard, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(uiState.levelInfo.title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Lv. ${uiState.levelInfo.level}", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            LinearProgressIndicator(
                progress = { uiState.levelInfo.progressPercent / 100f },
                color = PrimaryBlue,
                trackColor = BgSurface,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
            )
            Text(
                "${uiState.levelInfo.expIntoLevel} / ${uiState.levelInfo.expNeededForLevel} EXP",
                color = TextSub,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        // Preferensi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .background(BgCard, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto Rotate Layar", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Layar otomatis landscape pas video di-fullscreen-in", color = TextSub, fontSize = 11.sp)
            }
            Switch(
                checked = uiState.autoRotateEnabled,
                onCheckedChange = { viewModel.setAutoRotate(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.signOut(onSignedOut) },
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Keluar dari Akun", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
        }
    }
}
