package com.suikamovie.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suikamovie.app.ui.theme.BgCard
import com.suikamovie.app.ui.theme.BgBody
import com.suikamovie.app.ui.theme.DangerRed
import com.suikamovie.app.ui.theme.PrimaryBlue
import com.suikamovie.app.ui.theme.TextLight
import com.suikamovie.app.ui.theme.TextMain
import com.suikamovie.app.ui.theme.TextSub

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBody)
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(18.dp))
                .padding(24.dp),
        ) {
            Text(
                "SUIKAMOVIE",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Text(
                if (uiState.mode == AuthMode.LOGIN) "Login" else "Daftar Akun Baru",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 6.dp),
            )
            Text(
                if (uiState.mode == AuthMode.LOGIN)
                    "Login pake akun Google atau email buat lanjut nonton."
                else
                    "Bikin akun baru pake nama, email, dan password.",
                color = TextSub,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 18.dp),
            )

            // Tombol Google Sign-In
            OutlinedButton(
                onClick = { viewModel.signInWithGoogle(onLoggedIn) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Lanjutkan dengan Google", color = TextMain, fontWeight = FontWeight.SemiBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("atau", color = TextLight, fontSize = 12.sp)
            }

            if (uiState.mode == AuthMode.REGISTER) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    colors = suikaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                colors = suikaTextFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = suikaTextFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            )

            uiState.errorMessage?.let { msg ->
                Text(msg, color = DangerRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
            }

            Button(
                onClick = {
                    if (uiState.mode == AuthMode.LOGIN) {
                        viewModel.signInWithEmail(email, password, onLoggedIn)
                    } else {
                        viewModel.registerWithEmail(name, email, password, onLoggedIn)
                    }
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 6.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp).width(20.dp))
                } else {
                    Text(if (uiState.mode == AuthMode.LOGIN) "Login" else "Daftar", fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(
                onClick = {
                    viewModel.setMode(if (uiState.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    if (uiState.mode == AuthMode.LOGIN) "Belum punya akun? Daftar" else "Udah punya akun? Login",
                    color = PrimaryBlue,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun suikaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = TextLight,
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = TextLight,
    cursorColor = PrimaryBlue,
)
