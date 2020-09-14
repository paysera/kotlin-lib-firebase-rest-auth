package com.paysera.lib.firebaserestauth.auth

import android.os.Handler
import com.google.firebase.auth.internal.IdTokenListener
import com.google.firebase.internal.InternalTokenResult

class FirebaseTokenRefresher(
    private val restAuth: FirebaseRestAuth
) : IdTokenListener {

    private var lastToken: String? = null
    private val handler: Handler = Handler()
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (restAuth.currentUser == null) return

            val diffSecs = restAuth.currentUser?.expirationInSeconds?.minus(TEN_MINUTES_SECS) ?: 0L
            if (diffSecs > 0) {
                handler.postDelayed(this, diffSecs * 1000)
                return
            }

            restAuth.getAccessToken(true).addOnSuccessListener {
                handler.post(this)
            }.addOnFailureListener {
                handler.postDelayed(this, RETRY_MILLS)
            }
        }
    }

    override fun onIdTokenChanged(internalTokenResult: InternalTokenResult) {
        if (internalTokenResult.token != null && lastToken == null) {
            handler.post(refreshRunnable)
        }

        if (lastToken != null && internalTokenResult.token == null) {
            handler.removeCallbacksAndMessages(null)
        }
        lastToken = internalTokenResult.token
    }

    fun start() {
        restAuth.addIdTokenListener(this)
        handler.post(refreshRunnable)
    }

    fun stop() {
        restAuth.removeIdTokenListener(this)
        handler.removeCallbacksAndMessages(this)
    }

    companion object {
        const val TEN_MINUTES_SECS = 10 * 60
        const val RETRY_MILLS: Long = 60 * 1000
    }
}