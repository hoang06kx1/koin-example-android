package com.hoang.survey.listsurveys

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hoang.survey.api.ApiResponse
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.base.BaseViewModel
import com.hoang.survey.repository.SurveyRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import retrofit2.HttpException

class MainActivityViewModel(
    private val surveyRepository: SurveyRepository,
    private val initialLoadRequest: Int,
    private val perPageItems: Int
) : BaseViewModel() {
    val OFFSET_TO_LOAD_MORE = 2  // Identify when trigger loading more data

    private val _surveysLiveData = MutableLiveData<List<SurveyItemResponse>>()
    val surveysLiveData: LiveData<List<SurveyItemResponse>> = _surveysLiveData
    private var isLoadingSurveys = false
    private var hasMoreToLoad = true

    init {
        _surveysLiveData.value = listOf()
    }

    /**
     * Lazy loading to increase UX, because we don't know how many items available at first so we just load a small number of items.
     * More items will be loaded automatically when user scroll to the end
     */
    fun getSurveysLazy() {
        hasMoreToLoad = true
        Flowable.range(1, initialLoadRequest)
            .concatMap {
                surveyRepository.getSurveys(it, perPageItems).toFlowable()
            }  // concatMap here gives better performance comparing to concatMapEager
            .takeUntil {
                hasMoreToLoad = !(it.isEmpty() || it.size < perPageItems)
                !hasMoreToLoad
            }
            .doOnSubscribe {
                isLoadingSurveys = true
                _loading.value = true
            }
            .doOnTerminate { isLoadingSurveys = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _loading.value = false
                val mutableList = ArrayList(_surveysLiveData.value)
                mutableList.addAll(it)
                _surveysLiveData.value = mutableList
            }, { err ->
                _loading.value = false
                err.printStackTrace()
                handleApiError(err)
            }).addTo(disposables)
    }

    /**
     * Refresh surveys which are displayed on the screen.
     * If has no survey, reload all initial surveys
     */
    fun refreshSurvey() {
        if (surveysLiveData.value!!.isEmpty()) { // first load failed, reload data
            getSurveysLazy()
        } else { // update exising data
            surveyRepository.getSurveys(1, _surveysLiveData.value!!.size)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : FullCallbackWrapper<List<SurveyItemResponse>>() {
                    override fun onResponse(response: ApiResponse<List<SurveyItemResponse>>) {
                        response.result?.let {
                            _surveysLiveData.value = it
                        }
                    }
                }).addTo(disposables)
        }
    }

    /***
     * Load more item when user slide to near end
     */
    fun handleLoadMoreSurveys(currentItemPosition: Int) {
        if (!isLoadingSurveys && _surveysLiveData.value!!.size - currentItemPosition <= OFFSET_TO_LOAD_MORE && hasMoreToLoad && currentItemPosition != 0) {
            isLoadingSurveys = true
            val nextPageToLoad = (_surveysLiveData.value!!.size / perPageItems) + 1
            surveyRepository.getSurveys(nextPageToLoad, perPageItems)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : RawCallbackWrapper<List<SurveyItemResponse>>() {
                    override fun onResponse(response: ApiResponse<List<SurveyItemResponse>>) {
                        response.result?.let {
                            if (it.isNotEmpty()) {
                                val mutableList = ArrayList(_surveysLiveData.value)
                                mutableList.addAll(it)
                                _surveysLiveData.value = mutableList
                            }
                            if (it.isEmpty() || it.size < perPageItems) {
                                hasMoreToLoad = false
                            }
                        }
                        if (response.throwable != null && response.throwable !is HttpException) {
                            hasMoreToLoad = false
                        }
                        isLoadingSurveys = false
                    }
                }).addTo(disposables)
        }
    }
}