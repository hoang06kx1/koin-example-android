package com.hoang.survey.listsurveys

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blankj.utilcode.util.Utils
import com.google.gson.JsonSyntaxException
import com.hoang.survey.R
import com.hoang.survey.api.SurveyItemResponse
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.testutil.RxImmediateSchedulerRule
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Single
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import retrofit2.HttpException
import retrofit2.Response
import javax.net.ssl.HttpsURLConnection

@RunWith(AndroidJUnit4::class)
class MainActivityViewModelTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    var testSchedulerRule = RxImmediateSchedulerRule()

    private val surveyRepository: SurveyRepository = mock()
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val fakeSurvey = SurveyItemResponse("id", "title", "desc", "url")
    private val INITIAL_LOAD_REQUESTS = 1
    private val PER_PAGE_ITEMS = 4

    @Before
    fun init() {
        mainActivityViewModel = MainActivityViewModel(surveyRepository, INITIAL_LOAD_REQUESTS, PER_PAGE_ITEMS)
        whenever(surveyRepository.getSurveys(any(), any()))
            .thenAnswer { invocation ->
                val perPage = invocation.getArgument<Int>(1)
                return@thenAnswer Single.just(createFakeListSurveys(perPage))
            }
    }

    private fun createFakeListSurveys(size: Int): List<SurveyItemResponse> {
        val fakeSurveys = arrayListOf<SurveyItemResponse>()
        for (i in 1..size) {
            fakeSurveys.add(fakeSurvey.copy())
        }
        return fakeSurveys
    }

    @Test
    fun `Test null`() {
        mainActivityViewModel.surveysLiveData `should not be` null
        mainActivityViewModel.apiErrorMessage `should not be` null
        mainActivityViewModel._loading `should not be` null
    }

    @Test
    fun `Test http errors`() {
        stubHttpError(HttpsURLConnection.HTTP_UNAUTHORIZED)
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.apiErrorMessage.value `should equal` Utils.getApp().getString(R.string.unauthorized_user)

        stubHttpError(HttpsURLConnection.HTTP_FORBIDDEN)
        mainActivityViewModel.refreshSurvey()
        mainActivityViewModel.apiErrorMessage.value `should equal` Utils.getApp().getString(R.string.forbidden)

        stubHttpError(HttpsURLConnection.HTTP_INTERNAL_ERROR)
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.apiErrorMessage.value `should equal` Utils.getApp().getString(R.string.internal_server_error)

        stubHttpError(HttpsURLConnection.HTTP_BAD_REQUEST)
        mainActivityViewModel.refreshSurvey()
        mainActivityViewModel.apiErrorMessage.value `should equal` Utils.getApp().getString(R.string.bad_request)
    }

    @Test
    fun `Test Json error`() {
        whenever(surveyRepository.getSurveys(any(), any())).thenReturn(
            Single.error(
                JsonSyntaxException("")
            )
        )
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.apiErrorMessage.value `should equal` Utils.getApp().getString(R.string.wrong_data_format)
    }

    @Test
    fun `Test exception error`() {
        whenever(surveyRepository.getSurveys(any(), any())).thenReturn(
            Single.error(
                Exception("Random exception")
            )
        )
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.apiErrorMessage.value `should equal` "Random exception"
    }

    @Test
    fun `When lazy load surveys at the first time, only load predefined numbers of items`() {
        val sizeShouldLoad = INITIAL_LOAD_REQUESTS * PER_PAGE_ITEMS
        mainActivityViewModel.getSurveysLazy()
        for (i in 1..INITIAL_LOAD_REQUESTS) {
            verify(surveyRepository, times(1)).getSurveys(i, PER_PAGE_ITEMS)
        }
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
    }

    @Test
    fun `When lazy load surveys at the first time, stop firing more requests when all available surveys are loaded`() {
        mainActivityViewModel = MainActivityViewModel(surveyRepository, 5, 4)
        // Stub
        reset(surveyRepository)
        whenever(surveyRepository.getSurveys(any(), any()))
            .thenReturn(Single.just(createFakeListSurveys(PER_PAGE_ITEMS)))
            .thenReturn(Single.just(createFakeListSurveys(PER_PAGE_ITEMS)))
            .thenReturn(Single.just(listOf(fakeSurvey))) // should stop request after 3 times
            .thenReturn(Single.just(listOf()))

        mainActivityViewModel.getSurveysLazy()
        for (i in 1..3) {
            verify(surveyRepository, times(1)).getSurveys(i, PER_PAGE_ITEMS)
        }
        verify(surveyRepository, times(0)).getSurveys(4, PER_PAGE_ITEMS)
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` (PER_PAGE_ITEMS * 2 + 1)
    }

    @Test
    fun `When user scroll to the end, should load more data`() {
        val sizeShouldLoad = INITIAL_LOAD_REQUESTS * PER_PAGE_ITEMS
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
        val observer = mock<Observer<List<SurveyItemResponse>>>()
        mainActivityViewModel.surveysLiveData.observeForever(observer) // first onchanged fired (1)

        var offset = sizeShouldLoad - mainActivityViewModel.OFFSET_TO_LOAD_MORE - 1 // not near enough to load
        mainActivityViewModel.handleLoadMoreSurveys(offset)
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad

        offset++ // trigger load more now
        mainActivityViewModel.handleLoadMoreSurveys(offset) // second onchanged fired (2)
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad + PER_PAGE_ITEMS

        offset++ // not near end anymore, don't trigger
        mainActivityViewModel.handleLoadMoreSurveys(offset) // don't trigger load more (2)
        verify(observer, times(2)).onChanged(any())
    }

    @Test
    fun `Do not load more data when user's current page is 0`() {
        val observer = mock<Observer<List<SurveyItemResponse>>>()
        mainActivityViewModel.surveysLiveData.observeForever(observer) // first onchanged fired
        mainActivityViewModel.handleLoadMoreSurveys(0)
        verify(observer, times(1)).onChanged(any())
    }

    @Test
    fun `Do not load more data if there is no more surveys available`() {
        mainActivityViewModel = MainActivityViewModel(surveyRepository, 5, PER_PAGE_ITEMS)
        // Stub
        reset(surveyRepository)
        whenever(surveyRepository.getSurveys(any(), any()))
            .thenReturn(Single.just(createFakeListSurveys(PER_PAGE_ITEMS)))
            .thenReturn(Single.just(createFakeListSurveys(PER_PAGE_ITEMS)))
            .thenReturn(Single.just(listOf(fakeSurvey))) // should stop request after 3 times
            .thenReturn(Single.just(listOf()))

        val observer = mock<Observer<List<SurveyItemResponse>>>()
        mainActivityViewModel.surveysLiveData.observeForever(observer) // first onchanged fired (1)
        mainActivityViewModel.getSurveysLazy() // onchanged fires 3 times (4)
        mainActivityViewModel.handleLoadMoreSurveys(mainActivityViewModel.surveysLiveData.value!!.size - 1) // should not trigger load more, there no more data

        mainActivityViewModel.surveysLiveData.value!!.size `should equal` (PER_PAGE_ITEMS * 2 + 1)
        verify(observer, times(4)).onChanged(any())
    }

    @Test
    fun `When refresh surveys, should call first lazy load if there is no data is displaying`() {
        val sizeShouldLoad = INITIAL_LOAD_REQUESTS * PER_PAGE_ITEMS
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` 0
        mainActivityViewModel.refreshSurvey()
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
    }

    @Test
    fun `When refresh surveys, load number of items equals with number of items are displaying`() {
        val sizeShouldLoad = INITIAL_LOAD_REQUESTS * PER_PAGE_ITEMS
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` 0
        val observer = mock<Observer<List<SurveyItemResponse>>>()
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
        mainActivityViewModel.surveysLiveData.observeForever(observer) // first onChanged (1)

        mainActivityViewModel.refreshSurvey() // second onChanged (2)
        val sizeShouldRefresh = sizeShouldLoad
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldRefresh
        verify(observer, times(2)).onChanged(any()) // changed 2 times: first lazy load and later refresh

        mainActivityViewModel.handleLoadMoreSurveys(sizeShouldLoad - 1) // third onChanged (3)
        mainActivityViewModel.refreshSurvey() // fourth onChanged (4)
        verify(observer, times(4)).onChanged(any())
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad + PER_PAGE_ITEMS
    }

    @After
    fun reset() {
        reset(surveyRepository)
    }

    private fun stubHttpError(code: Int) {
        val dump = "application/json"
        whenever(surveyRepository.getSurveys(any(), any())).thenReturn(
            Single.error(
                HttpException(
                    Response.error<ResponseBody>(
                        code,
                        dump.toResponseBody()
                    )
                )
            )
        )
    }
}