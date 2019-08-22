package com.hoang.survey.authentication

import com.hoang.survey.repository.SurveyRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.KoinComponent
import org.koin.core.inject

class AccessTokenAuthenticator(
    private val tokenProvider: AccessTokenProvider
) : Authenticator, KoinComponent {
    private val surveyRepository: SurveyRepository by inject()

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val newToken = tokenProvider.getToken()

            // Only proceed if previous request was made as an authentication request
            response.request.url.queryParameter("access_token")?.apply {
                val oldToken = response.request.url.queryParameter("access_token")
                // If the token has changed since the request was made, use the new token.
                if (newToken != oldToken) {
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

                val responseBody = surveyRepository.refreshToken().execute()
                val updatedToken = responseBody.body()?.accessToken
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
