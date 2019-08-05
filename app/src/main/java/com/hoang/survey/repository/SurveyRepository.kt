package com.hoang.survey.repository

import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.api.SurveyServiceApi
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.Call

interface SurveyRepository {
    fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>>
    fun refreshToken(refreshTokenUrl: String): Call<JSONObject>
}

class SurveyRepositoryImpl(private val surveyServiceApi: SurveyServiceApi) : SurveyRepository {
    override fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>> {
        return surveyServiceApi.getSurveys(page, perPage)
    }

    override fun refreshToken(refreshTokenUrl: String): Call<JSONObject> {
        return surveyServiceApi.refreshToken(refreshTokenUrl)
    }
}