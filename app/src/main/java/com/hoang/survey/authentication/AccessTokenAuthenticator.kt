package com.hoang.survey.authentication

import com.hoang.survey.di.REFRESH_TOKEN_ENDPOINT
import com.hoang.survey.repository.SurveyRepository
import com.hoang.survey.repository.SurveyRepositoryImpl
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    val tokenProvider: AccessTokenProvider,
    private val surveyRepositoryHolder: SurveyRepositoryHolder
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // We need to have a token in order to refresh it.
        val token = tokenProvider.getToken()

        synchronized(this) {
            val newToken = tokenProvider.getToken()

            // Check if the request made was previously made as an authenticated request.
            if (response.request.url.queryParameter("access_token") != null) {
                // If the token has changed since the request was made, use the new token.
                if (newToken != token) {
                    val originUrl = response.request.url
                    val newUrl = originUrl.newBuilder()
                        .removeAllQueryParameters("access_token")
                        .addQueryParameter("access_token", newToken)
                        .build()
                    return response.request
                        .newBuilder()
                        .url(newUrl)
                        .build()
                }

                val updatedTokenResponse =
                    surveyRepositoryHolder.surveyRepository.refreshToken(refreshTokenUrl = REFRESH_TOKEN_ENDPOINT)
                        .execute()
                val updatedToken = updatedTokenResponse.body()?.getString("access_token")
                if (updatedToken.isNullOrBlank()) return null
                tokenProvider.saveToken(updatedToken)
                val originUrl = response.request.url
                val newUrl = originUrl.newBuilder()
                    .removeAllQueryParameters("access_token")
                    .addQueryParameter("access_token", updatedToken)
                    .build()
                // Retry the request with the new token.
                return response.request
                    .newBuilder()
                    .url(newUrl)
                    .build()
            }
        }
        return null
    }
}

class SurveyRepositoryHolder private constructor() {
    companion object {
        private var instance: SurveyRepositoryHolder? = null
        fun getInstance(): SurveyRepositoryHolder =
            instance ?: synchronized(this) {
                instance ?: SurveyRepositoryHolder()
            }
    }

    lateinit var surveyRepository: SurveyRepository
}
