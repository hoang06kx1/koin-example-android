package com.hoang.survey.authentication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import com.scottyab.aescrypt.AESCrypt
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import java.lang.StringBuilder
import java.util.*

@RunWith(AndroidJUnit4::class)

class TokenProviderTest {

    val FILENAME = "preftest"
    val SECRETKEY = "secretkey"
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var context: Context
    lateinit var tokenProvider: TokenProvider
    val fakeToken =
        "mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0"
    var encryptedToken = ""
    var decryptedToken = ""

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE)
        encryptedToken = AESCrypt.encrypt(SECRETKEY, fakeToken)
        decryptedToken = AESCrypt.decrypt(SECRETKEY, encryptedToken)

        // Ensure no shared preferences have leaked from previous tests
        assertThat(sharedPreferences.all).hasSize(0)
        tokenProvider = TokenProvider.getInstance(sharedPreferences, SECRETKEY)

        // Should return empty at this init stage
        assertThat(tokenProvider.getToken()).isEmpty()
    }

    @Test
    fun `save token then get token should return same value`() {
        tokenProvider.saveToken(fakeToken)
        assertThat(tokenProvider.getToken()).isEqualTo(fakeToken)

        tokenProvider.saveToken("$fakeToken$fakeToken") // update new token
        tokenProvider.getToken()
        tokenProvider.getToken()
        assertThat(tokenProvider.getToken()).isEqualTo("$fakeToken$fakeToken") // token is updated & get token many times don't affect the return value
    }

    @Test
    fun `save empty token should be fine`() {
        tokenProvider.saveToken("")
        assertThat(tokenProvider.getToken()).isEmpty()
    }

    @Test
    fun `save longest token should be fine`() {
        val longToken = StringBuilder().apply {
            repeat(2000) {append('a')}
        }.toString()
        assertThat(longToken.length).isEqualTo(2000)
        tokenProvider.saveToken(longToken)
        assertThat(tokenProvider.getToken()).isEqualTo(longToken)
    }

    @Test
    fun `token should be encrypted and save to sharedPreference`() {
        tokenProvider.saveToken(fakeToken)
        assertThat(sharedPreferences.getString(TokenProvider.TOKEN_KEY, "")).isEqualTo(encryptedToken)
    }

    @Test
    fun `token should be decrypted after read from sharedPreference`() {
        sharedPreferences.edit().putString(TokenProvider.TOKEN_KEY, encryptedToken).commit()
        assertThat(tokenProvider.getToken()).isEqualTo(fakeToken)
    }

    @Test
    fun `token should be read from memory after being saved`() {
        val mockSharedPreferences = Mockito.mock(SharedPreferences::class.java, RETURNS_DEEP_STUBS)
        whenever(mockSharedPreferences.edit().putString(any(), any()).commit()).thenReturn(true)
        val tokenProvider = TokenProvider.getInstance(mockSharedPreferences, SECRETKEY)
        tokenProvider.saveToken(fakeToken)
        tokenProvider.getToken()
        verify(mockSharedPreferences, times(0)).getString(any(), any())
    }

    @Test
    fun `token should be saved to memory after read from sharedPreference`() {
        val mockSharedPreferences: SharedPreferences = mock()
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(encryptedToken)
        val tokenProvider = TokenProvider.getInstance(mockSharedPreferences, SECRETKEY)
        tokenProvider.getToken() // read from sharedPrefence
        tokenProvider.getToken() // read from memory
        tokenProvider.getToken() // read from memory
        verify(mockSharedPreferences, times(1)).getString(any(), any())
    }
}