package com.hoang.survey

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.hoang.survey.di.productionModule
import com.hoang.survey.di.sharableModule
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class SurveyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // core utils
        Utils.init(this)

        // error handler for rxjava2 to avoid crash ONLY ON RELEASE BUILD to increase User experience
        if (!BuildConfig.DEBUG) {
            RxJavaPlugins.setErrorHandler { err ->
                err.printStackTrace()
                // should log to a error-tracking server or to crash report system likes Crashlyptics
            }
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin()
    }

    protected open fun startKoin() {
        // DI
        // this check is for RoboElectric tests that run in parallel so Koin gets set up multiple times
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger()
                androidContext(this@SurveyApplication)
                modules(listOf(sharableModule, productionModule))
            }
        }
    }
}