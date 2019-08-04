package com.hoang.survey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.repository.SurveyRepository
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
