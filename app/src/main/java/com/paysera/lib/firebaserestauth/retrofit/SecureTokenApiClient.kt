package com.paysera.lib.firebaserestauth.retrofit

import com.paysera.lib.firebaserestauth.entities.ExchangeTokenRequest
import com.paysera.lib.firebaserestauth.entities.ExchangeTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SecureTokenApiClient {
    @POST("token")
    suspend fun exchangeRefreshToken(@Body exchangeTokenRequest: ExchangeTokenRequest) : ExchangeTokenResponse
}