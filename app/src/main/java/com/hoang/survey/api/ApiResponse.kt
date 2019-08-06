package com.hoang.survey.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ApiResponse<T>(val result: T?, val throwable: Throwable?)

data class SurveyItemResponse (
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val tille: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("cover_image_url")
    val coverImageUrlSmall: String = ""
): Serializable {
    val coverImageUrlBig: String
        get() = if (!coverImageUrlSmall.isBlank()) coverImageUrlSmall.plus("l") else ""
}

