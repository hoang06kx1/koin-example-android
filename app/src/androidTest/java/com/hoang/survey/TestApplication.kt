package com.hoang.survey

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner
import com.hoang.survey.di.sharableModule
import com.hoang.survey.di.testModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TestApplication: SurveyApplication() {
    override fun startKoin() {
        org.koin.core.context.startKoin {
            androidLogger()
            androidContext(this@TestApplication)
            modules(listOf(sharableModule, testModule))
        }
    }
}

class MockTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle?) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        super.onCreate(arguments)
    }

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}