package com.suikamovie.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Web client ID dari android/app/google-services.json (oauth_client dengan
// client_type 3) - WAJIB dipake buat GetGoogleIdOption.serverClientId,
// BUKAN Android client ID (client_type 1).
private const val WEB_CLIENT_ID =
    "342381718186-apnu0qnqlt0tk900q245cmktq9otisch.apps.googleusercontent.com"

/**
 * Autentikasi native pake Firebase Auth SDK langsung (pengganti Firebase JS
 * SDK yang dulu jalan di dalam WebView). Login Google pake Credential
 * Manager - API modern penerus GoogleSignInClient yang udah deprecated.
 */
class AuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Emit user tiap kali status login berubah (login/logout/token refresh). */
    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(): Result<FirebaseUser> = runCatching {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(context, request)
        val credential = response.credential

        val googleIdTokenCredential = if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data)
        } else {
            throw IllegalStateException("Tipe credential Google nggak dikenali")
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        authResult.user ?: throw IllegalStateException("Login Google gagal: user null")
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user ?: throw IllegalStateException("Login gagal: user null")
    }

    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Registrasi gagal: user null")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
        user
    }

    fun signOut() {
        auth.signOut()
    }

    /** Port dari friendlyAuthError() di auth.js - ubah pesan error Firebase jadi bahasa manusia. */
    fun friendlyErrorMessage(err: Throwable): String {
        val msg = err.message ?: return "Terjadi kesalahan, coba lagi."
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("password is invalid") || msg.contains("no user record") ->
                "Email atau password salah."
            msg.contains("email-already-in-use") || msg.contains("EMAIL_EXISTS") ->
                "Email ini udah terdaftar. Coba login."
            msg.contains("weak-password") || msg.contains("WEAK_PASSWORD") ->
                "Password terlalu lemah, minimal 6 karakter."
            msg.contains("invalid-email") || msg.contains("INVALID_EMAIL") ->
                "Format email nggak valid."
            msg.contains("network") ->
                "Koneksi internet bermasalah, coba lagi."
            else -> "Terjadi kesalahan: $msg"
        }
    }
}
