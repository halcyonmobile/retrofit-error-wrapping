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

/**
 * A Converter which maps a parsed error into a custom [RuntimeException]
 */
interface ParsedErrorToExceptionConverter<ParsedError> {

    /**
     * Converts the [model] parsed error into a custom [RuntimeException]
     */
    fun convert(model: ParsedError?): RuntimeException

    /**
     * Maps an error found while waiting for the response.
     */
    fun convert(throwable: Throwable): RuntimeException

    /**
     * Factory which returns the proper [ParsedErrorToExceptionConverter] by the type of the error which can be parsed.
     */
    interface Factory {
        fun <ParsedError> create(errorClass: Class<out ParsedError>?): ParsedErrorToExceptionConverter<*>
    }

    /**
     * Helper factory which just maps for each type a [ParsedErrorToExceptionConverter]
     */
    class DelegateFactory(parsedErrorToExceptionConvertersByTypes: Map<in Class<*>, ParsedErrorToExceptionConverter<out Any>> = mutableMapOf()) : Factory{
        private val parsedErrorToExceptionConvertersByTypes = parsedErrorToExceptionConvertersByTypes.toMutableMap()


        override fun <ParsedError> create(errorClass: Class<out ParsedError>?): ParsedErrorToExceptionConverter<*> =
            parsedErrorToExceptionConvertersByTypes[errorClass] ?: throw IllegalStateException("Didn't find converter for $errorClass")

        fun <Error: Any> add(error: Class<Error>, converter: ParsedErrorToExceptionConverter<Error>): DelegateFactory = apply {
            parsedErrorToExceptionConvertersByTypes[error] = converter
        }

    }
}

/**
 * Helper function to add [ParsedErrorToExceptionConverter] to the [ParsedErrorToExceptionConverter.DelegateFactory]
 */
inline fun <reified Error: Any> ParsedErrorToExceptionConverter.DelegateFactory.add(converter: ParsedErrorToExceptionConverter<Error>): ParsedErrorToExceptionConverter.DelegateFactory =
    add(Error::class.java, converter)