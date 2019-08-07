package com.hoang.survey

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner

class TestApplication: SurveyApplication() {
    override fun getApiEndpoint(): String {
        return "http://localhost:8080/"
    }

    override fun getInitialLoadRequest(): Int {
        return 1
    }

    override fun getItemsPerRequest(): Int {
        return 4
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