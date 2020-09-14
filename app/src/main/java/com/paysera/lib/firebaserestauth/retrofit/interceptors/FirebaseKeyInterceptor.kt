package com.paysera.lib.firebaserestauth.retrofit.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class FirebaseKeyInterceptor(
    private val webApiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("key", this.webApiKey)
            .build()

        val requestBuilder = request.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept-Encoding", "identity")
            .url(newUrl)

        return chain.proceed(requestBuilder.build())
    }
}