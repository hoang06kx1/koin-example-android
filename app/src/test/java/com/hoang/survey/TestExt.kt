package com.hoang.survey

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Okio
import okio.buffer
import okio.source
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

fun MockWebServer.takeRequestWithTimeout(timeout:Long = 5L): RecordedRequest? {
    return this.takeRequest(timeout, TimeUnit.SECONDS)
}