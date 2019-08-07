package com.hoang.survey.testutil

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
import org.mockito.ArgumentCaptor
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

inline fun <reified T : Any> argumentCaptor() = ArgumentCaptor.forClass(T::class.java)

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
