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

    @Before
    fun init() {
        mainActivityViewModel = MainActivityViewModel(surveyRepository)
        whenever(surveyRepository.getSurveys(any(), any()))
            .thenAnswer { invocation ->
                val perPage = invocation.getArgument<Int>(1)
                return@thenAnswer Single.just(createFakeListSurveys(perPage))
            }
        mainActivityViewModel.PER_PAGE_ITEMS `should be greater than` 0
        mainActivityViewModel.INITIAL_LOAD_REQUESTS `should be greater than` 0
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
        val sizeShouldLoad = mainActivityViewModel.INITIAL_LOAD_REQUESTS * mainActivityViewModel.PER_PAGE_ITEMS
        mainActivityViewModel.getSurveysLazy()
        for (i in 1..mainActivityViewModel.INITIAL_LOAD_REQUESTS) {
            verify(surveyRepository, times(1)).getSurveys(i, mainActivityViewModel.PER_PAGE_ITEMS)
        }
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
    }

    @Test
    fun `When lazy load surveys at the first time, stop fire more requests when all available surveys are loaded`() {
        // Set number of initial load requests
        val requestsNumber = mainActivityViewModel.javaClass.getDeclaredField("INITIAL_LOAD_REQUESTS")
        requestsNumber.isAccessible = true
        requestsNumber.set(mainActivityViewModel, 5)
        mainActivityViewModel.INITIAL_LOAD_REQUESTS `should equal` 5

        // Stub
        reset(surveyRepository)
        whenever(surveyRepository.getSurveys(any(), any()))
            .thenReturn(Single.just(createFakeListSurveys(mainActivityViewModel.PER_PAGE_ITEMS)))
            .thenReturn(Single.just(createFakeListSurveys(mainActivityViewModel.PER_PAGE_ITEMS)))
            .thenReturn(Single.just(listOf(fakeSurvey))) // should stop request after 3 times
            .thenReturn(Single.just(listOf()))

        mainActivityViewModel.getSurveysLazy()
        for (i in 1..3) {
            verify(surveyRepository, times(1)).getSurveys(i, mainActivityViewModel.PER_PAGE_ITEMS)
        }
        verify(surveyRepository, times(0)).getSurveys(4, mainActivityViewModel.PER_PAGE_ITEMS)
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` (mainActivityViewModel.PER_PAGE_ITEMS * 2 + 1)
    }

    @Test
    fun `When refresh surveys, should call first lazy load if there is no data is displaying`() {
        val sizeShouldLoad = mainActivityViewModel.INITIAL_LOAD_REQUESTS * mainActivityViewModel.PER_PAGE_ITEMS
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` 0
        mainActivityViewModel.refreshSurvey()
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
    }

    @Test
    fun `When refresh surveys, load number of items equals with number of items are displaying`() {
        val sizeShouldLoad = mainActivityViewModel.INITIAL_LOAD_REQUESTS * mainActivityViewModel.PER_PAGE_ITEMS
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` 0
        val observer = mock<Observer<List<SurveyItemResponse>>>()
        mainActivityViewModel.getSurveysLazy()
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldLoad
        mainActivityViewModel.surveysLiveData.observeForever(observer)
        mainActivityViewModel.refreshSurvey()
        val sizeShouldRefresh = sizeShouldLoad
        mainActivityViewModel.surveysLiveData.value!!.size `should equal` sizeShouldRefresh
        verify(observer, times(2)).onChanged(any()) // changed 2 times: first lazy load and later refresh
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