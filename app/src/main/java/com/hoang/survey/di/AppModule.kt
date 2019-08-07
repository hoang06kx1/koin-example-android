package com.hoang.survey.di

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hoang.survey.BuildConfig
import com.hoang.survey.api.SurveyServiceApi
import com.hoang.survey.authentication.AccessTokenAuthenticator
import com.hoang.survey.authentication.AccessTokenProvider
import com.hoang.survey.authentication.SurveyRepositoryHolder
import com.hoang.survey.listsurveys.MainActivityViewModel
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.repository.SurveyRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val DEFAULT_NETWORK_TIMEOUT = 10L
val API_ENDPOINT = "https://nimble-survey-api.herokuapp.com/surveys.json/"
val REFRESH_TOKEN_ENDPOINT =
    "https://nimble-survey-api.herokuapp.com/oauth/token?grant_type=password&username=carlos%40nimbl3.com&password=antikera"

val testableModule = module {
    single { providesOkHttpClient(accessTokenProvider = get()) }
    single { provideGson() }
    single { AccessTokenProvider.getInstance(pref = get(), secretKey = DeviceUtils.getAndroidID()) }
    viewModel { MainActivityViewModel(get()) }
}

val frameworkModule = module {
    single { provideSurveyRepositoryImpl(get(), REFRESH_TOKEN_ENDPOINT)}
    single { androidApplication().getSharedPreferences("com.hoang.survey.pref", MODE_PRIVATE) as SharedPreferences }
    single { providesRetrofitAdapter(httpClient = get(), gson = get(), endPoint = API_ENDPOINT, refreshTokenEndpoint = REFRESH_TOKEN_ENDPOINT) }
}

fun provideGson(): Gson {
    return GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
}

fun providesRetrofitAdapter(gson: Gson, httpClient: OkHttpClient, endPoint: String, refreshTokenEndpoint: String): Retrofit {
    val retrofit = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(endPoint)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .build()

    // inject survey repository for refresh token
    if (SurveyRepositoryHolder.getInstance().surveyRepository == null) {
        SurveyRepositoryHolder.getInstance().surveyRepository =
            SurveyRepositoryImpl(
                retrofit.create(SurveyServiceApi::class.java),
                refreshTokenEndpoint
            )
    }
    return retrofit
}

fun providesOkHttpClient(accessTokenProvider: AccessTokenProvider): OkHttpClient {
    val logInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
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

    return OkHttpClient.Builder().apply {
        readTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        addInterceptor(tokenInterceptor)
        authenticator(AccessTokenAuthenticator(accessTokenProvider, SurveyRepositoryHolder.getInstance()))
        addInterceptor(logInterceptor)
    }.build()
}

fun provideSurveyRepositoryImpl(retrofit: Retrofit, refreshTokenEndpoint: String): SurveyRepository {
    if (SurveyRepositoryHolder.getInstance().surveyRepository == null) {
        val surveyRepository = SurveyRepositoryImpl(
            retrofit.create(SurveyServiceApi::class.java),
            refreshTokenEndpoint
        )
        SurveyRepositoryHolder.getInstance().surveyRepository = surveyRepository
    }
    return SurveyRepositoryHolder.getInstance().surveyRepository!!
}