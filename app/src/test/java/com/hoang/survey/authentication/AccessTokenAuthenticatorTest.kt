package com.hoang.survey.authentication

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hoang.survey.di.*
import com.hoang.survey.testutil.enqueueFromFile
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.testutil.resetSingleton
import com.hoang.survey.testutil.takeRequestWithTimeout
import com.nhaarman.mockito_kotlin.notNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.koin.android.ext.koin.androidApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.get
import retrofit2.Retrofit
import kotlin.test.assertNotNull


@RunWith(AndroidJUnit4::class)
class AccessTokenAuthenticatorTest : KoinTest {
    val FAKE_NEW_TOKEN = "thisisjustafaketokenfortesting"
    lateinit var server: MockWebServer
    lateinit var accessTokenProvider: AccessTokenProvider
    lateinit var refresTokenEndpoint: String
    lateinit var repository: SurveyRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start(8080)

        resetSingleton(AccessTokenProvider::class.java)
        resetSingleton(SurveyRepositoryHolder::class.java)
        loadKoinModules(
            listOf(
                module {
                    single(override = true) {
                        providesRetrofitAdapter(
                            httpClient = get(),
                            gson = get(),
                            endPoint = "http://127.0.0.1:8080/",
                            refreshTokenEndpoint = "http://127.0.0.1:8080/refresh/token"
                        )
                    }
                })
        )
        refresTokenEndpoint = "http://localhost:8080/refresh/token"
        accessTokenProvider = get()
        repository = get()
    }

    @Test
    fun `SurveyRepositoryHolder should be singleton`() {
        assertThat(SurveyRepositoryHolder.getInstance()).isEqualTo(SurveyRepositoryHolder.getInstance())
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
        repository.getSurveys(1, 1).subscribe()

        // Check requests
        server.takeRequestWithTimeout()                                     // first survey request, get 401 response
        val refreshTokenRequest = server.takeRequestWithTimeout()           // request to get new token
        val surveyRequest = server.takeRequestWithTimeout()                 // should retry request with new token

        repository.getSurveys(1, 1).subscribe()                // second times
        val surveyRequest2 = server.takeRequestWithTimeout()                // should not call request new token

        assertThat(accessTokenProvider.getToken()).isEqualTo(FAKE_NEW_TOKEN)                                        // new token is updated
        assertThat(refreshTokenRequest!!.requestUrl.toString()).isEqualTo(refresTokenEndpoint)                      // refresh token is called
        assertThat(surveyRequest!!.requestUrl!!.toString()).isNotEqualTo(refresTokenEndpoint)                       // retried request with new token
        assertThat(surveyRequest.requestUrl!!.queryParameter("access_token")).isEqualTo(FAKE_NEW_TOKEN)       // use new access token
        assertThat(surveyRequest2!!.requestUrl!!.queryParameter("access_token")).isEqualTo(FAKE_NEW_TOKEN)    // reuse new access token, don't request new token
    }

    @After
    fun tearDown() {
        server.shutdown()
        stopKoin()
    }
}