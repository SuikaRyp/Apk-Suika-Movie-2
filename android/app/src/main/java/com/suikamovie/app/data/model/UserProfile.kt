package com.suikamovie.app.data.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoURL: String,
    val exp: Int,
    val watchCount: Int,
)
