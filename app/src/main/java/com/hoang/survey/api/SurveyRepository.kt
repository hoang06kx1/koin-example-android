package com.hoang.survey.api

import com.hoang.survey.authentication.TokenResponse
import io.reactivex.Single
import retrofit2.Call

interface SurveyRepository {
    fun getAllSurveys(): Single<List<SurveyItemResponse>>
    fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>>
    fun refreshToken(): Call<TokenResponse>
}

class SurveyRepositoryImpl(private val surveyServiceApi: SurveyServiceApi, private val refreshTokenUrl: String) :
    SurveyRepository {
    override fun getAllSurveys(): Single<List<SurveyItemResponse>> {
        return surveyServiceApi.getAllSurveys()
    }

    override fun getSurveys(page: Int, perPage: Int): Single<List<SurveyItemResponse>> {
        return surveyServiceApi.getSurveys(page, perPage)
    }

    override fun refreshToken(): Call<TokenResponse> {
        return surveyServiceApi.refreshToken(refreshTokenUrl)
    }
}