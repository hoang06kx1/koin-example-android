package com.hoang.survey.listsurveys

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.hoang.survey.R
import com.hoang.survey.authentication.AccessTokenProvider
import com.hoang.survey.authentication.SurveyRepositoryHolder
import com.hoang.survey.di.provideGson
import com.hoang.survey.di.provideSurveyRepositoryImpl
import com.hoang.survey.di.providesOkHttpClient
import com.hoang.survey.di.providesRetrofitAdapter
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.surveydetail.SurveyDetailActivity
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.ext.getFullName
import retrofit2.Retrofit

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)
//    @get:Rule
//    @JvmField
//    val executorRule = TaskExecutorWithIdlingResourceRule()
//
//    @Rule
//    @JvmField
//    val countingAppExecutors = CountingAppExecutorsRule()

    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences
    lateinit var server: MockWebServer
    lateinit var testUrl: String
    lateinit var accessTokenProvider: AccessTokenProvider
    lateinit var refresTokenEndpoint: String
    lateinit var okHttpClient: OkHttpClient
    lateinit var retrofit: Retrofit
    lateinit var repository: SurveyRepository
    lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun initActivity() {
//        resetSingleton(AccessTokenProvider::class.java)
//        resetSingleton(SurveyRepositoryHolder::class.java)

        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences("FILENAME", Context.MODE_PRIVATE)
        server = MockWebServer()
        testUrl = server.url("/").toString()
        accessTokenProvider = AccessTokenProvider.getInstance(sharedPreferences, "SECRETKEY")
        refresTokenEndpoint = "${testUrl}refresh/token"
        okHttpClient = providesOkHttpClient(accessTokenProvider)
        retrofit = providesRetrofitAdapter(provideGson(), okHttpClient, testUrl, refresTokenEndpoint)
        repository = provideSurveyRepositoryImpl(retrofit, refresTokenEndpoint)

    }

    @Test
    fun clickTakeSurveyButton_shouldNavigateToNextScreen() {
        scenario = rule.scenario
        onView(withId(R.id.bt_take_survey)).check(matches(withText(R.string.take_the_survey)))
        onView(withId(R.id.bt_take_survey)).perform(click())
        intended(hasComponent(SurveyDetailActivity::class.getFullName()))
    }
}