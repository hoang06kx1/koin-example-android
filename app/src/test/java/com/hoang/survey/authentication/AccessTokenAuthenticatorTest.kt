package com.hoang.survey.authentication

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blankj.utilcode.util.DeviceUtils
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.hoang.survey.BuildConfig
import com.hoang.survey.api.SurveyServiceApi
import com.hoang.survey.di.*
import com.hoang.survey.repository.SurveyRepository
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.get
import org.koin.test.inject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class AccessTokenAuthenticatorTest : KoinTest {
    val FILENAME = "preftest"
    val SECRETKEY = "secretkey"
    lateinit var context: Context
    lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        context = ApplicationProvider.getApplicationContext()

    }


    @Test
    fun `When fails 401 unauthorized, Then call refresh token`() {
        val testUrl = server.url("/")
        val testRunnerModule = module {
            single { providesOkHttpClient(accessTokenProvider = get()) }
            single { provideGson() }
            single<SurveyRepository> { provideSurveyRepositoryImpl(retrofit = get()) }
            single { AccessTokenProvider.getInstance(pref = get(), secretKey = "123123123") }
            single { context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE) as SharedPreferences}
            single { providesRetrofitAdapter(httpClient = get(), gson = get(), endPoint = testUrl.toString()) }
        }
        startKoin {
            modules(testRunnerModule)
        }.checkModules()

        val fakeRefreshToken = "asdfkvchksdfklm_ksifhwefewbf"
        val invalidTokenResponse = MockResponse().setResponseCode(401)
        val responseNewToken = "{\"access_token\":\"$fakeRefreshToken\"}"
        val refreshTokenResponse = MockResponse().setResponseCode(200).setBody(responseNewToken)
        val successResponse = MockResponse().setResponseCode(200)

        server.enqueue(invalidTokenResponse)
        server.enqueue(refreshTokenResponse)
        server.enqueue(successResponse)

        val accessTokenProvider = get<AccessTokenProvider>()
        assertThat(accessTokenProvider.getToken()).isEqualTo(fakeRefreshToken)
    }

    @After
    fun tearDown() {
        stopKoin()
        server.shutdown()
    }
}