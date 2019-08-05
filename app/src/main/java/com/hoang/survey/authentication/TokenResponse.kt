package com.hoang.survey.authentication

import com.google.gson.annotations.SerializedName

data class TokenResponse(@SerializedName("access_token") val accessToken:String)