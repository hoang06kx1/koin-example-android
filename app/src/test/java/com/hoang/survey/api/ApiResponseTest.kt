package com.hoang.survey.api

import org.amshove.kluent.`should equal`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ApiResponseTest {
    @Test
    fun highResolutionImageUrl() {
        val smallImageUrl = "http://smallimage.com/1/2_"
        val survey = SurveyItemResponse(coverImageUrlSmall = smallImageUrl)
        survey.coverImageUrlBig `should equal` "${survey.coverImageUrlSmall}l"
    }
}