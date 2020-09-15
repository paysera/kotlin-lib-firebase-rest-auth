package com.paysera.lib.firebaserestauth.utils

import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.FirebaseException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

object IdTokenParser {

    fun parseIdToken(idToken: String): Map<String, Any> {
        val parts = idToken.split(".").toList()
        if (parts.size < 2) {
            return mapOf()
        }

        val encodedToken = parts[1]
        return try {
            val decodedToken =
                String(Base64Utils.decodeUrlSafeNoPadding(encodedToken), Charset.defaultCharset())
            parseRawUserInfo(decodedToken) ?: mapOf()
        } catch (e: UnsupportedEncodingException) {
            mapOf()
        }
    }

    private fun parseRawUserInfo(rawUserInfo: String): Map<String, Any>? {
        if (rawUserInfo.isNullOrEmpty()) return null

        try {
            val jsonObject = JSONObject(rawUserInfo)
            return if (jsonObject !== JSONObject.NULL) {
                toMap(jsonObject)
            } else {
                null
            }
        } catch (e: Exception) {
            throw FirebaseException(e.message!!)
        }
    }

    @Throws(JSONException::class)
    fun toMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keyItr = json.keys()
        while (keyItr.hasNext()) {
            val key = keyItr.next()

            var value = json.get(key)
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }

            map[key] = value
        }
        return map
    }

    @Throws(JSONException::class)
    fun toList(array: JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
            var value = array.get(i)
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            list.add(value)
        }
        return list
    }
}