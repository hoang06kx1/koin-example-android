package com.hoang.survey.listsurveys

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.viewpager2.widget.ViewPager2
import com.google.common.truth.Truth.assertThat
import com.hoang.survey.R
import com.hoang.survey.base.EspressoCountingIdlingResource
import com.hoang.survey.di.sharableModule
import com.hoang.survey.di.testModule
import com.hoang.survey.surveydetail.SurveyDetailActivity
import com.hoang.survey.testutil.enqueueFromFile
import com.hoang.survey.testutil.swipeNext
import com.hoang.survey.testutil.swipePrevious
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.internal.matcher.DisplayedMatchers.displayedAssignableFrom
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ext.getFullName

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)
    val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        mockWebServer.enqueueFromFile("surveys-4.json")
        IdlingRegistry.getInstance().register(EspressoCountingIdlingResource.idlingResource)
    }

    @Test
    fun errorToast_shouldBeShown() {
        mockWebServer.enqueue(MockResponse().apply { setResponseCode(500) })
        ActivityScenario.launch(MainActivity::class.java)
        SystemClock.sleep(2000)
        clickOn(R.id.bt_refresh)
        onView(withText(R.string.internal_server_error))
            .inRoot(withDecorView(not(MainActivity.getInstance().get()!!.window.decorView)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickTakeSurveyButton_shouldNavigateToNextScreen() {
        ActivityScenario.launch(MainActivity::class.java)
        clickOn(R.id.bt_take_survey)
        intended(hasComponent(SurveyDetailActivity::class.getFullName()))
    }

    @Test
    fun clickRefreshButton_shouldUpdateSurveysWithNewData() {
        mockWebServer.enqueueFromFile("surveys-4-refresh.json")
        ActivityScenario.launch(MainActivity::class.java)
        assertContains("Bangkok")
        clickOn(R.id.bt_refresh)
        assertContains("Danang")
    }

    @Test
    fun clickRefreshButton_dontChangeCurrentPagerPosition() {
        mockWebServer.enqueueFromFile("surveys-4-refresh.json")
        ActivityScenario.launch(MainActivity::class.java)
        onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipeNext())
        assertContains("Bangkok 2")
        clickOn(R.id.bt_refresh)
        assertContains("Danang 2")
    }

    @Test
    fun swipeDown_shouldNavigateToNextPage() { // vertical viewpager
        ActivityScenario.launch(MainActivity::class.java)
        assertContains("Bangkok 1")
        onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipeNext())
        assertContains("Bangkok 2")
        onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipePrevious())
        assertContains("Bangkok 1")
    }

    @Test
    fun swipeToEnd_shouldLoadMoreDataIfAvailable() {
        mockWebServer.enqueueFromFile("surveys-1-loadmore.json")
        ActivityScenario.launch(MainActivity::class.java)
        for (i in 1..4) {
            onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipeNext()) // reach the end, load more should be triggered before
        }
        onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipeNext())
        assertContains("Bangkok 5") // new data
    }

    @Test
    fun rotateScreen_shouldKeepCurrentPosition() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(displayedAssignableFrom(ViewPager2::class.java)).perform(swipeNext())
        assertThat(MainActivity.getInstance().get()!!.mainActivityViewModel.surveysLiveData.value!!.size).isEqualTo(4)
        assertContains("Bangkok 2")
        rotateScreen()
        SystemClock.sleep(1000) // wait for recreate
        assertContains("Bangkok 2")
        assertThat(MainActivity.getInstance().get()!!.mainActivityViewModel.surveysLiveData.value!!.size).isEqualTo(4) // don't trigger loading more data
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoCountingIdlingResource.idlingResource)
        mockWebServer.shutdown()
    }

    private fun rotateScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val orientation = context.resources.configuration.orientation
        val activity = MainActivity.getInstance().get()!!
        activity.requestedOrientation = if (orientation == Configuration.ORIENTATION_PORTRAIT)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}