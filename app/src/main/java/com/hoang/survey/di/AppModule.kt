package com.hoang.survey.di

import android.content.Context.MODE_PRIVATE
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hoang.survey.BuildConfig
import com.hoang.survey.api.SurveyServiceApi
import com.hoang.survey.authentication.AccessTokenAuthenticator
import com.hoang.survey.authentication.AccessTokenProvider
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.repository.SurveyRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.get
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val DEFAULT_NETWORK_TIMEOUT = 10L
val API_ENDPOINT = "https://nimble-survey-api.herokuapp.com/surveys.json/"

val appModule = module {
    single { providesOkHttpClient(get()) }
    single { providesRetrofitAdapter(get(), get()) }
    single { provideGson() }
    single<SurveyRepository> { provideSurveyRepositoryImpl(get()) }
    single { AccessTokenProvider.getInstance(get(), DeviceUtils.getAndroidID()) }
    single { androidApplication().getSharedPreferences("com.hoang.survey.pref", MODE_PRIVATE) }
}

private fun provideGson(): Gson {
    return GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
}

private fun providesRetrofitAdapter(gson: Gson, httpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .client(httpClient)
        .baseUrl(API_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .build()
}

private fun providesOkHttpClient(accessTokenProvider: AccessTokenProvider): OkHttpClient {
    val logInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    val tokenInterceptor = Interceptor {
        var request = it.request()
        val url = request.url.newBuilder().addQueryParameter("access_token", accessTokenProvider.getToken()).build()
        request = request.newBuilder().url(url).build()
        it.proceed(request)
    }

    return OkHttpClient.Builder().apply {
        readTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        addInterceptor(logInterceptor)
        addInterceptor(tokenInterceptor)
        authenticator(AccessTokenAuthenticator(accessTokenProvider))
    }.build()
}

private fun provideSurveyRepositoryImpl(retrofit: Retrofit): SurveyRepositoryImpl {
    return SurveyRepositoryImpl(retrofit.create(SurveyServiceApi::class.java))
}