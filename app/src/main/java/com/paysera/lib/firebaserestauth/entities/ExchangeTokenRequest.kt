package com.paysera.lib.firebaserestauth.entities

import com.google.gson.annotations.SerializedName

data class ExchangeTokenRequest(
    @SerializedName("grant_type")
    var grantType: String = "refresh_token",
    @SerializedName("refresh_token")
    var refreshToken: String
)