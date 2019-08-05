package com.hoang.survey.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val response: T?, val throwable: Throwable?) {
    var result = response
    var error = throwable
}

data class SurveyItemResponse(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val tille: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("cover_image_url")
    val coverImageUrlSmall: String = ""
) {
    val coverImageUrlBig: String
        get() = if (!coverImageUrlSmall.isBlank()) coverImageUrlSmall.plus("l") else ""
}

