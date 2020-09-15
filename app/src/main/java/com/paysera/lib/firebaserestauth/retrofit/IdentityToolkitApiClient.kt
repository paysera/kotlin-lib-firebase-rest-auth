package com.paysera.lib.firebaserestauth.retrofit

import com.paysera.lib.firebaserestauth.entities.SignInResponse
import com.paysera.lib.firebaserestauth.entities.SignInWithCustomTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface IdentityToolkitApiClient {

    @POST("v1/accounts:signUp")
    suspend fun signInAnonymously(): SignInResponse

    @POST("v1/accounts:signInWithCustomToken")
    suspend fun signInWithCustomToken(@Body customTokenRequest: SignInWithCustomTokenRequest) : SignInResponse
}