package com.hoang.survey.di

import com.hoang.survey.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
val DEFAULT_NETWORK_TIMEOUT = 10L
val appModule = module {
    single { providesOkHttpClient() }
}

private fun providesOkHttpClient(): OkHttpClient {
    val logInterceptor = HttpLoggingInterceptor()
    logInterceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

    return OkHttpClient.Builder().apply {
        readTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_NETWORK_TIMEOUT, TimeUnit.SECONDS)
        addInterceptor(logInterceptor)
    }.build()
}
