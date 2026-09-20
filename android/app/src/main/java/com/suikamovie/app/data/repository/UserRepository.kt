package com.suikamovie.app.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.suikamovie.app.data.model.UserProfile
import kotlinx.coroutines.tasks.await

/** Email yang otomatis dianggap OWNER/ADMIN aplikasi (port dari user-data.js). */
private val ADMIN_EMAILS = listOf("ciulbotak25@gmail.com")

fun isAdminEmail(email: String?): Boolean =
    !email.isNullOrBlank() && ADMIN_EMAILS.contains(email.lowercase())

/**
 * Profil user & sistem EXP/level, disimpen di Firestore koleksi "users"
 * (1 dokumen per UID) - port dari public/js/user-data.js.
 */
class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private fun userDoc(uid: String) = db.collection("users").document(uid)

    suspend fun ensureUserProfile(user: FirebaseUser): UserProfile {
        val ref = userDoc(user.uid)
        val snap = ref.get().await()

        if (!snap.exists()) {
            val fresh = mapOf(
                "uid" to user.uid,
                "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: "Pengguna"),
                "email" to (user.email ?: ""),
                "photoURL" to (user.photoUrl?.toString() ?: ""),
                "exp" to 0,
                "watchCount" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "lastLoginAt" to FieldValue.serverTimestamp(),
            )
            ref.set(fresh).await()
            return UserProfile(user.uid, fresh["displayName"] as String, fresh["email"] as String, fresh["photoURL"] as String, 0, 0)
        }

        // Sinkronin data profil dasar (siapa tau ganti nama/foto dari provider login)
        val updates = mapOf(
            "displayName" to (user.displayName ?: snap.getString("displayName") ?: "Pengguna"),
            "email" to (user.email ?: snap.getString("email") ?: ""),
            "photoURL" to (user.photoUrl?.toString() ?: snap.getString("photoURL") ?: ""),
            "lastLoginAt" to FieldValue.serverTimestamp(),
        )
        ref.update(updates).await()

        return UserProfile(
            uid = user.uid,
            displayName = updates["displayName"] as String,
            email = updates["email"] as String,
            photoURL = updates["photoURL"] as String,
            exp = (snap.getLong("exp") ?: 0L).toInt(),
            watchCount = (snap.getLong("watchCount") ?: 0L).toInt(),
        )
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        val snap = userDoc(uid).get().await()
        if (!snap.exists()) return null
        return UserProfile(
            uid = uid,
            displayName = snap.getString("displayName") ?: "Pengguna",
            email = snap.getString("email") ?: "",
            photoURL = snap.getString("photoURL") ?: "",
            exp = (snap.getLong("exp") ?: 0L).toInt(),
            watchCount = (snap.getLong("watchCount") ?: 0L).toInt(),
        )
    }

    /** Dipanggil tiap user mulai nonton film/episode -> nambah EXP. */
    suspend fun awardWatchExp(uid: String, amount: Int = com.suikamovie.app.data.model.EXP_PER_WATCH) {
        runCatching {
            userDoc(uid).update(
                mapOf(
                    "exp" to FieldValue.increment(amount.toLong()),
                    "watchCount" to FieldValue.increment(1),
                    "lastWatchAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }
}
