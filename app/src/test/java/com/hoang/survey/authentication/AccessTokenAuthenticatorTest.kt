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
    val okHttpClient: OkHttpClient by inject()
    val surveyRepository: SurveyRepository by inject()

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
//            single<SurveyRepository> { provideSurveyRepositoryImpl(retrofit = get()) }
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
        assertThat(accessTokenProvider).isNotNull()
        val logInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val tokenInterceptor = Interceptor {
            var request = it.request()
            if (request.url.pathSegments.lastOrNull()?.toString() == "token") { // don't add token in case refresh token is called
                it.proceed(request)
            } else {
                val url = request.url.newBuilder().addQueryParameter("access_token", accessTokenProvider.getToken()).build()
                request = request.newBuilder().url(url).build()
                it.proceed(request)
            }
        }

//        val okHttp = OkHttpClient.Builder().apply {
//            readTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
//            writeTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
//            addInterceptor(tokenInterceptor)
//            authenticator(AccessTokenAuthenticator(accessTokenProvider))
//            addInterceptor(logInterceptor)
//        }.build()

//        val retrofit = Retrofit.Builder()
//            .client(okHttp)
//            .baseUrl(server.url("/").toString())
//            .addConverterFactory(GsonConverterFactory.create(Gson()))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .build()

        val retrofit: Retrofit by inject()
        val testApi = retrofit.create(SurveyServiceApi::class.java)
        testApi.getSurveys(1,10).subscribe()


        val request1 = server.takeRequest()
        val request2 = server.takeRequest()
        val request3 = server.takeRequest()

        assertThat(accessTokenProvider.getToken()).isEqualTo(fakeRefreshToken)
    }

    @After
    fun tearDown() {
        stopKoin()
        server.shutdown()
    }
}