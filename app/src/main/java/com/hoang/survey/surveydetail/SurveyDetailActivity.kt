package com.hoang.survey.surveydetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hoang.survey.R
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.base.BaseActivity
import kotlinx.android.synthetic.main.activity_survey_detail.*

class SurveyDetailActivity : AppCompatActivity() {

    companion object {
        fun getIntent(originActivity: AppCompatActivity, item: SurveyItemResponse): Intent {
            return Intent(originActivity, SurveyDetailActivity::class.java).apply {
                putExtra("SURVEY_ITEM", item)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_detail)
        tv_survey_title.text = (intent.getSerializableExtra("SURVEY_ITEM") as SurveyItemResponse).tille
    }
}
