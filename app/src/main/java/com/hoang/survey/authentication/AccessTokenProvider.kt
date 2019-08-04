package com.hoang.survey.authentication

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.scottyab.aescrypt.AESCrypt

class AccessTokenProvider private constructor(
    private val sharedPreferences: SharedPreferences,
    private val secretKey: String
) {
    companion object {
        val TOKEN_KEY = "TOKEN_KEY"
        private var instance: AccessTokenProvider? = null
        fun getInstance(pref: SharedPreferences, secretKey: String): AccessTokenProvider =
            instance ?: synchronized(this) {
                instance ?: AccessTokenProvider(pref, secretKey)
            }
    }

    private var currentToken = ""

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
            val encryptedToken = sharedPreferences.getString(TOKEN_KEY, "")
            if (encryptedToken.isNullOrBlank()) return ""
            currentToken = AESCrypt.decrypt(secretKey, encryptedToken)
            return currentToken
        }
    }
}