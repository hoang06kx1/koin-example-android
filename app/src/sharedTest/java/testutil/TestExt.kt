package com.hoang.survey.testutil

import android.view.View
import androidx.core.view.ViewCompat
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.buffer
import okio.source
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.TimeUnit

fun MockWebServer.enqueueFromFile(fileName: String, headers: Map<String, String> = emptyMap()) {
    val inputStream = javaClass.classLoader
        .getResourceAsStream("api-response/$fileName")
    val source = inputStream.source().buffer()
    val mockResponse = MockResponse()
    for ((key, value) in headers) {
        mockResponse.addHeader(key, value)
    }
    enqueue(
        mockResponse.setBody(source.readString(Charsets.UTF_8))
    )
}

fun MockWebServer.takeRequestWithTimeout(timeout: Long = 5L): RecordedRequest? {
    return this.takeRequest(timeout, TimeUnit.SECONDS)
}

fun <T> resetSingleton(klass: Class<T>) {
    val instance = klass.getDeclaredField("instance")
    instance.isAccessible = true
    instance.set(null, null)
}

/**
 * ViewAction that issues a swipe gesture on a [ViewPager2] to move that ViewPager2 to the next
 * page, taking orientation and layout direction into account.
 */
fun swipeNext(): ViewAction {
    return SwipeAction(SwipeAction.Direction.FORWARD)
}

/**
 * ViewAction that issues a swipe gesture on a [ViewPager2] to move that ViewPager2 to the previous
 * page, taking orientation and layout direction into account.
 */
fun swipePrevious(): ViewAction {
    return SwipeAction(SwipeAction.Direction.BACKWARD)
}

private class SwipeAction(val direction: Direction) : ViewAction {
    enum class Direction {
        FORWARD,
        BACKWARD
    }

    override fun getDescription(): String = "Swiping $direction"

    override fun getConstraints(): Matcher<View> =
        allOf(isAssignableFrom(ViewPager2::class.java), isDisplayingAtLeast(90))

    override fun perform(uiController: UiController, view: View) {
        val vp = view as ViewPager2
        val isForward = direction == Direction.FORWARD
        val swipeAction: ViewAction
        if (vp.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            swipeAction = if (isForward == vp.isRtl()) swipeRight() else swipeLeft()
        } else {
            swipeAction = if (isForward) swipeUp() else swipeDown()
        }
        swipeAction.perform(uiController, view)
    }

    private fun ViewPager2.isRtl(): Boolean {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
    }
}

class RxImmediateSchedulerRule : TestRule {

    override fun apply(base: Statement, d: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
                RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
                RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}
