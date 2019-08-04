package com.hoang.survey

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.hoang.survey.di.appModule
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SurveyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // core utils
        Utils.init(this)

        // error handler for rxjava2 to avoid crash
        if (!BuildConfig.DEBUG) {
            RxJavaPlugins.setErrorHandler(Throwable::printStackTrace)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // DI
        startKoin {
            androidContext(this@SurveyApplication)
            modules(appModule)
        }
    }
}