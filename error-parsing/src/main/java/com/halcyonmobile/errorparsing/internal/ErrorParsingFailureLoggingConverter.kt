package com.halcyonmobile.errorparsing.internal

import com.halcyonmobile.errorparsing.loggers.ErrorParsingFailureLogger
import okhttp3.ResponseBody
import retrofit2.Converter

class ErrorParsingFailureLoggingConverter(
    private val errorParsingFailureLogger: ErrorParsingFailureLogger,
    private val converter: Converter<ResponseBody, Any?>): Converter<ResponseBody, Any?>{
    override fun convert(value: ResponseBody): Any? {
        try {
            return converter.convert(value)
        } catch (throwable: Throwable) {
            errorParsingFailureLogger.log(throwable)
            throw throwable
        }
    }

}