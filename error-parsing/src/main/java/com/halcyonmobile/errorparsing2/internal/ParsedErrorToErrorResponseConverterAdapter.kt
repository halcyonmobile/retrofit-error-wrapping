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

package com.halcyonmobile.errorparsing2.internal

import com.halcyonmobile.errorparsing2.ErrorResponseToExceptionConverter
import com.halcyonmobile.errorparsing2.ParsedErrorToExceptionConverter
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.lang.reflect.Type

/**
 * Adapter which adapts a [ParsedErrorToExceptionConverter] into an [ErrorResponseToExceptionConverter] so it can be used in [ErrorWrappingAndParsingCall]
 */
internal class ParsedErrorToErrorResponseConverterAdapter<T, ParsedError>(
    private val parsedErrorToExceptionConverter: ParsedErrorToExceptionConverter<ParsedError>,
    private val converter: Converter<ResponseBody, ParsedError?>
) : ErrorResponseToExceptionConverter<T> {

    override fun convert(response: Response<T>): RuntimeException =
        parsedErrorToExceptionConverter.convert(response.errorBody()?.let(converter::convert))

    override fun convert(throwable: Throwable): RuntimeException =
        parsedErrorToExceptionConverter.convert(throwable)


    class Factory(private val parsedErrorToExceptionConverterFactory: ParsedErrorToExceptionConverter.Factory) : ErrorResponseToExceptionConverter.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <Data, Error> create(type: Type, errorClass: Class<out Any>?, converter: Converter<ResponseBody, Error?>): ErrorResponseToExceptionConverter<Data> =
            ParsedErrorToErrorResponseConverterAdapter(
                parsedErrorToExceptionConverter = parsedErrorToExceptionConverterFactory.create(errorClass as Class<out Error>?) as ParsedErrorToExceptionConverter<Error>,
                converter = converter
            )

    }
}