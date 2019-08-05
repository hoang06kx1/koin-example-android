package com.hoang.survey.authentication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hoang.survey.resetSingleton
import com.nhaarman.mockito_kotlin.*
import com.scottyab.aescrypt.AESCrypt
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS

@RunWith(AndroidJUnit4::class)
class AccessTokenProviderTest {

    val FILENAME = "preftest"
    val SECRETKEY = "secretkey"
    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var accessTokenProvider: AccessTokenProvider
    val FAKE_TOKEN =
        "mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0mmH5EI0cWpmEjihY0Ii0"
    var encryptedToken = ""
    var decryptedToken = ""

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences(FILENAME, MODE_PRIVATE)
        encryptedToken = AESCrypt.encrypt(SECRETKEY, FAKE_TOKEN)
        decryptedToken = AESCrypt.decrypt(SECRETKEY, encryptedToken)

        // Ensure no shared preferences have leaked from previous tests
        assertThat(sharedPreferences.all).hasSize(0)

        // Reset singleton
        resetSingleton(AccessTokenProvider::class.java)

        // Should return empty at this init stage
        accessTokenProvider = AccessTokenProvider.getInstance(sharedPreferences, SECRETKEY)
        assertThat(accessTokenProvider.getToken()).isEmpty()
    }

    @Test
    fun `AccessTokenProvider is singleton`() {
        assertThat(
            AccessTokenProvider.getInstance(
                sharedPreferences,
                SECRETKEY
            )
        ).isEqualTo(AccessTokenProvider.getInstance(sharedPreferences, SECRETKEY))
    }

    @Test
    fun `Save token then get token should return same value`() {
        accessTokenProvider.saveToken(FAKE_TOKEN)
        assertThat(accessTokenProvider.getToken()).isEqualTo(FAKE_TOKEN)

        accessTokenProvider.saveToken("$FAKE_TOKEN$FAKE_TOKEN") // update new token
        accessTokenProvider.getToken()
        accessTokenProvider.getToken()
        assertThat(accessTokenProvider.getToken()).isEqualTo("$FAKE_TOKEN$FAKE_TOKEN") // token is updated & get token many times don't affect the return value
    }

    @Test
    fun `Save empty token should be fine`() {
        accessTokenProvider.saveToken("")
        assertThat(accessTokenProvider.getToken()).isEmpty()
    }

    @Test
    fun `Save longest token should be fine`() {
        val longToken = StringBuilder().apply {
            repeat(2000) { append('a') }
        }.toString()
        assertThat(longToken.length).isEqualTo(2000)
        accessTokenProvider.saveToken(longToken)
        assertThat(accessTokenProvider.getToken()).isEqualTo(longToken)
    }

    @Test
    fun `Token should be encrypted and save to sharedPreference`() {
        accessTokenProvider.saveToken(FAKE_TOKEN)
        assertThat(sharedPreferences.getString(AccessTokenProvider.TOKEN_KEY, "")).isEqualTo(encryptedToken)
    }

    @Test
    fun `Token should be decrypted after read from sharedPreference`() {
        sharedPreferences.edit().putString(AccessTokenProvider.TOKEN_KEY, encryptedToken).commit()
        assertThat(accessTokenProvider.getToken()).isEqualTo(FAKE_TOKEN)
    }

    @Test
    fun `Token should be read from memory after being saved`() {
        val mockSharedPreferences = Mockito.mock(SharedPreferences::class.java, RETURNS_DEEP_STUBS)
        whenever(mockSharedPreferences.edit().putString(any(), any()).commit()).thenReturn(true)
        val tokenProvider = AccessTokenProvider.getInstance(mockSharedPreferences, SECRETKEY)
        tokenProvider.saveToken(FAKE_TOKEN)
        tokenProvider.getToken()
        verify(mockSharedPreferences, times(0)).getString(any(), any())
    }

    @Test
    fun `Token should be saved to memory after read from sharedPreference`() {
        val mockSharedPreferences: SharedPreferences = mock()
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(encryptedToken)
        // Reset singleton
        val instance = AccessTokenProvider::class.java!!.getDeclaredField("instance")
        instance.isAccessible = true
        instance.set(null, null)
        val tokenProvider = AccessTokenProvider.getInstance(mockSharedPreferences, SECRETKEY)
        tokenProvider.getToken() // read from sharedPrefence
        tokenProvider.getToken() // read from memory
        tokenProvider.getToken() // read from memory
        verify(mockSharedPreferences, times(1)).getString(any(), any())
    }
}