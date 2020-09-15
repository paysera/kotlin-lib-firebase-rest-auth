package com.paysera.lib.firebaserestauth.entities

data class SignInWithCustomTokenRequest(
    private val token: String,
    private val returnSecureToken: Boolean = true
)