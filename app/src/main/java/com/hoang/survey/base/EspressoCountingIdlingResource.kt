package com.hoang.survey.base

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoCountingIdlingResource {
    var idlingResource: CountingIdlingResource = CountingIdlingResource("loadingDialog")
}