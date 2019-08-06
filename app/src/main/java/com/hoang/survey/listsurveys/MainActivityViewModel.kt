package com.hoang.survey.listsurveys

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hoang.survey.api.ApiResponse
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.base.BaseViewModel
import com.hoang.survey.repository.SurveyRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo

class MainActivityViewModel(private val surveyRepository: SurveyRepository): BaseViewModel() {
    private val _surveysLiveData = MutableLiveData<List<SurveyItemResponse>>()
    val surveysLiveData: LiveData<List<SurveyItemResponse>> = _surveysLiveData

    fun getSurveys() {
        surveyRepository.getAllSurveys()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object: FullCallbackWrapper<List<SurveyItemResponse>>() {
                override fun onResponse(response: ApiResponse<List<SurveyItemResponse>>) {
                    response.result?.let {
                        _surveysLiveData.value = it
                    }
                }
            }).addTo(disposables)
    }
}