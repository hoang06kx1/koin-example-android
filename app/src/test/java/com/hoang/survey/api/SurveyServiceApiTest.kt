package com.hoang.survey.api

import com.hoang.survey.testutil.enqueueFromFile
import com.hoang.survey.testutil.takeRequestWithTimeout
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class SurveyServiceApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: SurveyServiceApi

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(SurveyServiceApi::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun getToken() {
        mockWebServer.enqueueFromFile("refresh-token.json")
        val response = service.refreshToken(mockWebServer.url("/").toString()).execute()
        response.body() `should not be` null
        response.body()!!.accessToken `should equal` "thisisjustafaketokenfortesting"
    }

    @Test
    fun getSurveys() {
        mockWebServer.enqueueFromFile("surveys-2.json")
        service.getSurveys(1,2)
            .test()
            .assertValue {
                val survey = it[0]
                survey.id == "d5de6a8f8f5f1cfe51bc"
                        && survey.tille == "Scarlett Bangkok"
                        && survey.description == "We'd love ot hear from you!"
                        && survey.coverImageUrlSmall == "https://dhdbhh0jsld0o.cloudfront.net/m/1ea51560991bcb7d00d0_"
                        && survey.coverImageUrlBig == "https://dhdbhh0jsld0o.cloudfront.net/m/1ea51560991bcb7d00d0_l"
            }
            .assertValue { it.size == 2 }

        val request = mockWebServer.takeRequestWithTimeout()!!
        request.requestUrl `should not be` null
        request.requestUrl!!.queryParameter("page") `should equal` "1"
        request.requestUrl!!.queryParameter("per_page") `should equal` "2"
    }
}