package com.paysera.lib.firebaserestauth.entities

import com.google.gson.annotations.SerializedName

data class ExchangeTokenResponse(
    @SerializedName("expires_in")
    val expiresIn: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("id_token")
    val idToken: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("project_id")
    val projectId: String
)
