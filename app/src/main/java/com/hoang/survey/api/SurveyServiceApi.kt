package com.hoang.survey.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SurveyServiceApi {
    @GET
    fun getAllSurveys(): Single<List<SurveyItemResponse>>

    @GET
    fun getSurveys(@Query("page") page: Int, @Query("per_page") perPage: Int): Single<List<SurveyItemResponse>>
}