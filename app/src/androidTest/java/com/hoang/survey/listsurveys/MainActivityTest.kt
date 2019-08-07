package com.hoang.survey.listsurveys

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.hoang.survey.R
import com.hoang.survey.base.EspressoCountingIdlingResource
import com.hoang.survey.surveydetail.SurveyDetailActivity
import com.hoang.survey.testutil.enqueueFromFile
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.ext.getFullName

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)
//    @get:Rule
//    @JvmField
//    val executorRule = TaskExecutorWithIdlingResourceRule()

    lateinit var scenario: ActivityScenario<MainActivity>
    val mockWebServer = MockWebServer()

    @Before
    fun initActivity() {
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(EspressoCountingIdlingResource.idlingResource)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun clickTakeSurveyButton_shouldNavigateToNextScreen() {
        mockWebServer.enqueueFromFile("surveys-1.json")
        clickOn(R.id.bt_take_survey)
        intended(hasComponent(SurveyDetailActivity::class.getFullName()))
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoCountingIdlingResource.idlingResource)
        mockWebServer.shutdown()
    }
}