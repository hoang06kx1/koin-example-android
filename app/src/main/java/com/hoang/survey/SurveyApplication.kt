package com.hoang.survey

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.hoang.survey.di.frameworkModule
import com.hoang.survey.di.testableModule
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber

open class SurveyApplication : Application() {
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
        // this check is for RoboElectric tests that run in parallel so Koin gets set up multiple times
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@SurveyApplication)
                modules(listOf(testableModule, frameworkModule))
            }
        }
    }

    open fun getApiEndpoint(): String {
        return "https://nimble-survey-api.herokuapp.com/surveys.json/"
    }

    open fun getInitialLoadRequest(): Int {
        return 2
    }

    open fun getItemsPerRequest(): Int {
        return 4
    }
}