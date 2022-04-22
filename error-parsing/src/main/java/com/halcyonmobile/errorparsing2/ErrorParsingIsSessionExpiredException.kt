package com.halcyonmobile.errorparsing2

import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException

/**
 * Custom implementation of [IsSessionExpiredException] which evaluates
 * if the custom throwable is a `SESSION_EXPIRED` exception or not.
 */
class ErrorParsingIsSessionExpiredException : IsSessionExpiredException {

    override fun invoke(throwable: Throwable): Boolean =
        when (throwable) {
            is NetworkException -> {
                throwable.isInvalidTokenException() || throwable.isExpiredTokenException()
            }
            else -> false
        }

    companion object {
        private fun NetworkException.isInvalidTokenException() = errorBody?.contains("\"Invalid refresh token:") ?: false

        private fun NetworkException.isExpiredTokenException() = errorBody?.contains("\"Invalid refresh token (expired):") ?: false

    }
}