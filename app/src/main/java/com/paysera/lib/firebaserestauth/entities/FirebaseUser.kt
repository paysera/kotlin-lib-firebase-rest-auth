package com.paysera.lib.firebaserestauth.entities

import com.paysera.lib.firebaserestauth.utils.IdTokenParser
import java.util.*

class FirebaseUser(
    val idToken: String,
    val refreshToken: String,
    val isAnonymous: Boolean = false
) {

    var userId: String? = null
    private var expirationTime: Long? = null
    val expirationInSeconds: Long
        get() {
            return expirationTime?.let {
                return@let it - (Date().time / 1000)
            } ?: 0L
        }

    init {
        val claims = IdTokenParser.parseIdToken(idToken)
        this.userId = claims["user_id"].toString()
        this.expirationTime = claims["exp"].toString().toLong()
    }
}