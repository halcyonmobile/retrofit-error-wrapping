package com.halcyonmobile.errorparsing.loggers

/**
 * Logger to log any exception while parsing an error model from errorBody.
 */
interface ErrorParsingFailureLogger {

    fun log(throwable: Throwable)
}