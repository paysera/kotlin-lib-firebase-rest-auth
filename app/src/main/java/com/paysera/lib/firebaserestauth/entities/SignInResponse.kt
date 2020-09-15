package com.paysera.lib.firebaserestauth.entities

data class SignInResponse(
    val kind: String,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String,
    val email: String,
    val registered: Boolean
)