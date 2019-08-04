package com.hoang.survey.repository

import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.api.SurveyServiceApi
import io.reactivex.Single

interface SurveyRepository {
    fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>>
}

class SurveyRepositoryImpl(val surveyServiceApi: SurveyServiceApi) : SurveyRepository {
    override fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>> {
        return surveyServiceApi.getSurveys(page, perPage)
    }
}