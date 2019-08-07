package com.hoang.survey.listsurveys

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.hoang.survey.R
import com.hoang.survey.base.EspressoCountingIdlingResource
import com.hoang.survey.surveydetail.SurveyDetailActivity
import com.hoang.survey.testutil.enqueueFromFile
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
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

    val mockWebServer = MockWebServer()

    @Before
    fun initActivity() {
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(EspressoCountingIdlingResource.idlingResource)
    }

    @Test
    fun clickTakeSurveyButton_shouldNavigateToNextScreen() {
        mockWebServer.enqueueFromFile("surveys-1.json")
        ActivityScenario.launch(MainActivity::class.java)
        clickOn(R.id.bt_take_survey)
        intended(hasComponent(SurveyDetailActivity::class.getFullName()))
    }

    @Test
    fun clickRefreshButton_shouldUpdateSurveys() {
        mockWebServer.enqueueFromFile("surveys-4.json")
        mockWebServer.enqueueFromFile("surveys-1.json")
        mockWebServer.enqueueFromFile("surveys-1.json")
        mockWebServer.enqueueFromFile("surveys-1-refresh.json")
        ActivityScenario.launch(MainActivity::class.java)
        val activity = MainActivity.getInstance().get()

        // Set number of initial load requests
        val requestsNumber = activity!!.mainActivityViewModel.javaClass.getDeclaredField("INITIAL_LOAD_REQUESTS")
        requestsNumber.isAccessible = true
        requestsNumber.set(activity!!.mainActivityViewModel, 1) // only load one time
        assertThat(activity!!.mainActivityViewModel.INITIAL_LOAD_REQUESTS).isEqualTo(1)

        // Set number of per page items request
        val requestItems = activity!!.mainActivityViewModel.javaClass.getDeclaredField("PER_PAGE_ITEMS")
        requestsNumber.isAccessible = true
        requestsNumber.set(activity!!.mainActivityViewModel, 1) // only load one time
        assertThat(activity!!.mainActivityViewModel.INITIAL_LOAD_REQUESTS).isEqualTo(4)

        assertContains("Bangkok")
        clickOn(R.id.bt_refresh)
        assertContains("Danang")
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoCountingIdlingResource.idlingResource)
        mockWebServer.shutdown()
    }
}