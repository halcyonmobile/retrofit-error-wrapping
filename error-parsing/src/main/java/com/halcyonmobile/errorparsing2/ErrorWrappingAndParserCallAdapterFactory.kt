/*
 * Copyright (c) 2020 Halcyon Mobile.
 * https://www.halcyonmobile.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.halcyonmobile.errorparsing2

import com.halcyonmobile.errorparsing2.internal.ErrorParsingFailureLoggingConverter
import com.halcyonmobile.errorparsing2.internal.ErrorWrappingAndParsingCall
import com.halcyonmobile.errorparsing2.internal.NetworkExceptionToErrorResponseConverterAdapter
import com.halcyonmobile.errorparsing2.internal.ParsedErrorToErrorResponseConverterAdapter
import com.halcyonmobile.errorparsing2.loggers.ErrorParsingFailureLogger
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Call adapter factory which can be used to wrap any error thrown by the request into a
 * [NetworkException] if the request is annotated with [WrapIntoNetworkException] or [ParsedError].
 *
 * If [ParsedError] annotation is used then the errorBody will also be parsed into the exception.
 * For the parsing process the [Retrofit.converterFactories] are used.
 */
class ErrorWrappingAndParserCallAdapterFactory(
    private val errorResponseToExceptionConverterFactory: ErrorResponseToExceptionConverter.Factory,
    private val workWithoutAnnotation: Boolean = false,
    private val errorParsingFailureLogger: ErrorParsingFailureLogger? = null
) : CallAdapter.Factory() {

    constructor(
        networkExceptionConverter: NetworkExceptionConverter = DummyNetworkExceptionConverter(),
        workWithoutAnnotation: Boolean = false,
        errorParsingFailureLogger: ErrorParsingFailureLogger? = null) : this(
        errorResponseToExceptionConverterFactory = NetworkExceptionToErrorResponseConverterAdapter.Factory(networkExceptionConverter),
        workWithoutAnnotation = workWithoutAnnotation,
        errorParsingFailureLogger = errorParsingFailureLogger
    )

    constructor(
        parsedErrorToExceptionConverterFactory: ParsedErrorToExceptionConverter.Factory,
        workWithoutAnnotation: Boolean = false,
        errorParsingFailureLogger: ErrorParsingFailureLogger? = null) : this(
        errorResponseToExceptionConverterFactory = ParsedErrorToErrorResponseConverterAdapter.Factory(parsedErrorToExceptionConverterFactory),
        workWithoutAnnotation = workWithoutAnnotation,
        errorParsingFailureLogger = errorParsingFailureLogger
    )

    @Suppress("UNCHECKED_CAST")
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val errorClass = annotations
            .asSequence()
            .filterIsInstance<ParsedError>()
            .map(ParsedError::value)
            .map { it.java }
            .firstOrNull()
        val converter = errorClass?.let { retrofit.responseBodyConverter<Any?>(it, annotations) }

        if (converter == null && annotations.none { it is WrapIntoNetworkException } && !workWithoutAnnotation) {
            return null
        }

        val nonNullConverter = converter ?: NullConverter()
        val wrappedConverter = errorParsingFailureLogger?.let { ErrorParsingFailureLoggingConverter(it, nonNullConverter) } ?: nonNullConverter

        val rawType = getParameterUpperBound(0, returnType as ParameterizedType)
        return Adapter<Any>(rawType, errorResponseToExceptionConverterFactory.create(rawType, errorClass, wrappedConverter))
    }

    class Adapter<T>(
        private val rawType: Type,
        private val errorResponseToExceptionConverter: ErrorResponseToExceptionConverter<T>
    ) : CallAdapter<T, Call<T>> {

        override fun adapt(call: Call<T>): Call<T> = ErrorWrappingAndParsingCall(call, errorResponseToExceptionConverter)

        override fun responseType(): Type = rawType
    }

    /**
     * Fake converter which always returns null.
     */
    class NullConverter : Converter<ResponseBody, Any?> {
        override fun convert(value: ResponseBody): Any? = null
    }
}