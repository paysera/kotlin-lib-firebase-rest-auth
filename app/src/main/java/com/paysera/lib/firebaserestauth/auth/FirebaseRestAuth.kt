package com.paysera.lib.firebaserestauth.auth

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.auth.internal.InternalAuthProvider
import com.google.firebase.internal.InternalTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.paysera.lib.firebaserestauth.entities.ExchangeTokenRequest
import com.paysera.lib.firebaserestauth.entities.ExchangeTokenResponse
import com.paysera.lib.firebaserestauth.entities.FirebaseUser
import com.paysera.lib.firebaserestauth.entities.SignInWithCustomTokenRequest
import com.paysera.lib.firebaserestauth.extensions.createApiClient
import com.paysera.lib.firebaserestauth.extensions.execute
import com.paysera.lib.firebaserestauth.extensions.launch
import com.paysera.lib.firebaserestauth.retrofit.IdentityToolkitApiClient
import com.paysera.lib.firebaserestauth.retrofit.SecureTokenApiClient
import com.paysera.lib.firebaserestauth.retrofit.interceptors.FirebaseKeyInterceptor
import com.paysera.lib.firebaserestauth.utils.IdTokenParser
import kotlinx.coroutines.GlobalScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class FirebaseRestAuth(
    firebaseApp: FirebaseApp,
    httpLoggingInterceptorLevel: HttpLoggingInterceptor.Level
) : InternalAuthProvider {

    var currentUser: FirebaseUser? = null
        set(value) {
            field = value

            if (!listeners.any { it is FirebaseTokenRefresher }) {
                tokenRefresher.start()
            }

            listeners.forEach {
                it.onIdTokenChanged(InternalTokenResult(value?.idToken))
            }
        }

    private val tokenRefresher = FirebaseTokenRefresher(this)
    private var identityToolkitApiClient: IdentityToolkitApiClient
    private var secureTokenApiClient: SecureTokenApiClient
    private var listeners = mutableListOf<IdTokenListener>()

    init {
        OkHttpClient.Builder()
            .addInterceptor(FirebaseKeyInterceptor(firebaseApp.options.apiKey))
            .addInterceptor(HttpLoggingInterceptor().setLevel(httpLoggingInterceptorLevel))
            .build()
            .also {
                identityToolkitApiClient =
                    it.createApiClient("https://identitytoolkit.googleapis.com/")
                secureTokenApiClient = it.createApiClient("https://securetoken.googleapis.com/v1/")
            }
    }

    override fun getUid() = currentUser?.userId

    override fun getAccessToken(forceRefresh: Boolean): Task<GetTokenResult> {
        val source = TaskCompletionSource<GetTokenResult>()
        if (currentUser != null) {
            val needsRefresh = forceRefresh || currentUser!!.expirationInSeconds <= 0
            if (!needsRefresh) {
                val getTokenResult = GetTokenResult(
                    currentUser!!.idToken,
                    IdTokenParser.parseIdToken(currentUser!!.idToken)
                )
                source.trySetResult(getTokenResult)
            } else {
                refreshUserToken(currentUser!!.refreshToken).addOnSuccessListener {
                    currentUser = FirebaseUser(
                        it!!.idToken,
                        it.refreshToken
                    )
                    val getTokenResult = GetTokenResult(
                        currentUser!!.idToken,
                        IdTokenParser.parseIdToken(currentUser!!.idToken)
                    )
                    source.trySetResult(getTokenResult)
                }.addOnFailureListener { exception ->
                    source.trySetException(exception)
                }
            }
        } else {
            source.trySetException(FirebaseNoSignedInUserException("No user signed-in"))
        }

        return source.task
    }

    override fun addIdTokenListener(listener: IdTokenListener) {
        listeners.add(listener)
    }

    override fun removeIdTokenListener(listener: IdTokenListener) {
        listeners.remove(listener)
    }

    fun stopTokenRefresher() {
        tokenRefresher.stop()
    }

    fun signInWithCustomToken(token: String): Task<FirebaseUser> {
        return TaskCompletionSource<FirebaseUser>().execute {
            val customTokenSignInRequest = SignInWithCustomTokenRequest(token)
            val data = identityToolkitApiClient.signInWithCustomToken(customTokenSignInRequest)
            FirebaseUser(data.idToken, data.refreshToken)
        }.task.addOnSuccessListener { firebaseUser ->
            this.currentUser = firebaseUser
        }
    }

    fun signInAnonymously(): Task<FirebaseUser> {
        return TaskCompletionSource<FirebaseUser>().execute {
            val data = identityToolkitApiClient.signInAnonymously()
            FirebaseUser(data.idToken, data.refreshToken, true)
        }.task.addOnSuccessListener { firebaseUser ->
            this.currentUser = firebaseUser
        }
    }

    private fun refreshUserToken(refreshToken: String): Task<ExchangeTokenResponse> {
        val source = TaskCompletionSource<ExchangeTokenResponse>()
        GlobalScope.launch(work = {
            secureTokenApiClient.exchangeRefreshToken(ExchangeTokenRequest(refreshToken = refreshToken))
        }, onSuccess = { exchangeTokenResponse ->
            source.trySetResult(exchangeTokenResponse)
        }, onError = { throwable ->
            source.trySetException(Exception(throwable))
        })
        return source.task
    }

    companion object {
        private var firebaseRestAuth: FirebaseRestAuth? = null

        fun getInstance(
            app: FirebaseApp,
            httpLoggingInterceptorLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BASIC
        ): FirebaseRestAuth {
            if (firebaseRestAuth == null) {
                firebaseRestAuth = FirebaseRestAuth(
                    app,
                    httpLoggingInterceptorLevel
                )
            }

            return firebaseRestAuth!!
        }
    }
}