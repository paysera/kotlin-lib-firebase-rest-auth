package com.paysera.lib.firebaserestauth.extensions

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

inline fun <reified T> OkHttpClient.createApiClient(
    apiUrl: String
): T {
    val retrofit = Retrofit.Builder()
        .baseUrl(apiUrl)
        .client(this)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    return retrofit.create(T::class.java)
}