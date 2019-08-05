package com.hoang.survey.authentication

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hoang.survey.di.*
import com.hoang.survey.enqueueFromFile
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.takeRequestWithTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class AccessTokenAuthenticatorTest : KoinTest {
    val FILENAME = "preftest3"
    val SECRET_KEY = "secretkey"
    val FAKE_NEW_TOKEN = "thisisjustafaketokenfortesting"

    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences
    lateinit var server: MockWebServer
    lateinit var testUrl: String
    lateinit var accessTokenProvider: AccessTokenProvider
    lateinit var refresTokenEndpoint: String
    lateinit var okHttpClient: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var repository: SurveyRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
        server = MockWebServer()
        testUrl = server.url("/").toString()
        accessTokenProvider = AccessTokenProvider.getInstance(sharedPreferences, SECRET_KEY)
        refresTokenEndpoint = "${testUrl}refresh/token"
        okHttpClient = providesOkHttpClient(accessTokenProvider)
        retrofit = providesRetrofitAdapter(provideGson(), okHttpClient, testUrl, refresTokenEndpoint)
        repository = provideSurveyRepositoryImpl(retrofit, refresTokenEndpoint)
    }

    @Test
    fun `When fails 401 unauthorized, Then call refresh token`() {
        assertThat(accessTokenProvider.getToken()).isEmpty()

        val invalidTokenResponse = MockResponse().setResponseCode(401)
        server.enqueue(invalidTokenResponse)
        server.enqueueFromFile("refresh-token.json")
        server.enqueueFromFile("surveys-1.json")
        server.enqueueFromFile("surveys-1.json")

        // Make requests
        repository.getSurveys(1,1).subscribe()

        // Check requests
        server.takeRequestWithTimeout()                                     // first survey request, get 401 response
        val refreshTokenRequest = server.takeRequestWithTimeout()           // request to get new token
        val surveyRequest = server.takeRequestWithTimeout()                 // should retry request with new token

        repository.getSurveys(1,1).subscribe()                // second times
        val surveyRequest2 = server.takeRequestWithTimeout()                // should not call request new token

        assertThat(accessTokenProvider.getToken()).isEqualTo(FAKE_NEW_TOKEN)                                        // new token is updated
        assertThat(refreshTokenRequest!!.requestUrl.toString()).isEqualTo(refresTokenEndpoint)                      // refresh token is called
        assertThat(surveyRequest!!.requestUrl!!.toString()).isNotEqualTo(refresTokenEndpoint)                       // retried request with new token
        assertThat(surveyRequest.requestUrl!!.queryParameter("access_token")).isEqualTo(FAKE_NEW_TOKEN)       // use new access token
        assertThat(surveyRequest2!!.requestUrl!!.queryParameter("access_token")).isEqualTo(FAKE_NEW_TOKEN)    // reuse new access token, don't request new token
    }

    @After
    fun tearDown() {
        stopKoin()
        server.shutdown()
        // Reset singleton
        val instance = AccessTokenProvider::class.java!!.getDeclaredField("instance")
        instance.isAccessible = true
        instance.set(null, null)
        instance.set(null, null)
    }
}