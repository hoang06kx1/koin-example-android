package com.hoang.survey.listsurveys

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blankj.utilcode.util.Utils
import com.hoang.survey.R
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.testutil.RxImmediateSchedulerRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
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

    @Before
    fun init() {
        mainActivityViewModel = MainActivityViewModel(surveyRepository)
        reset(surveyRepository)
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

    private fun stubHttpError(code: Int) {
        val dump = "application/json"
        whenever(
            surveyRepository.getSurveys(
                any(),
                any()
            )
        ).thenReturn(
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