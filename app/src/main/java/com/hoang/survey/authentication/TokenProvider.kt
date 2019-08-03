package com.hoang.survey.authentication

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.scottyab.aescrypt.AESCrypt

class TokenProvider private constructor(
    private val sharedPreferences: SharedPreferences,
    private val secretKey: String
) {
    val TOKEN_KEY = "TOKEN_KEY"

    companion object {
        private var instance: TokenProvider? = null
        fun getInstance(pref: SharedPreferences, secretKey: String): TokenProvider =
            instance ?: synchronized(this) {
                instance ?: TokenProvider(pref, secretKey)
            }
    }

    var currentToken = ""

    @SuppressLint("ApplySharedPref")
    fun saveToken(token: String) {
        if (token.isNotBlank()) {
            // Add extra security layer to prevent data breaches
            val encryptedToken = AESCrypt.encrypt(secretKey, token)
            sharedPreferences.edit().putString(TOKEN_KEY, encryptedToken).commit()

            // Store plain token in memory
            currentToken = token
        }
    }

    fun getToken(): String {
        return if (currentToken.isNotBlank()) {
            currentToken
        } else {
            sharedPreferences.getString(TOKEN_KEY, "")?.let {
                return AESCrypt.decrypt(secretKey, it)
            } ?: ""
        }
    }
}