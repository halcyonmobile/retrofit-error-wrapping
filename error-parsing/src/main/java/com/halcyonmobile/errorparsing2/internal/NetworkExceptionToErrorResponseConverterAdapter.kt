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
import com.halcyonmobile.errorparsing2.NetworkException
import com.halcyonmobile.errorparsing2.NetworkExceptionConverter
import com.halcyonmobile.errorparsing2.NoNetworkException
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Type
import java.net.UnknownHostException

/**
 * Adapter which adapts a [NetworkExceptionConverter] into an [ErrorResponseToExceptionConverter] so it can be used in [ErrorWrappingAndParsingCall]
 */
internal class NetworkExceptionToErrorResponseConverterAdapter<T, Error>(
    private val converter: Converter<ResponseBody, Error?>,
    private val networkExceptionConverter: NetworkExceptionConverter
) : ErrorResponseToExceptionConverter<T> {

    override fun convert(response: Response<T>): RuntimeException {
        val (errorBodyForParsing, errorBodyAsString) = response.errorBody().getCopyAndString()
        val networkException = NetworkException(
            throwable = HttpException(response),
            parsedError = errorBodyForParsing?.let { converter.convertSafely(it) },
            errorBody = errorBodyAsString
        )

        return networkExceptionConverter.convert(networkException)
    }

    //UnknownHostException
    override fun convert(throwable: Throwable): RuntimeException =
        networkExceptionConverter.convert(throwable.convertToNetworkException())

    private fun Throwable.convertToNetworkException() : NetworkException=
        when(this){
            is NetworkException -> this
            is UnknownHostException -> NoNetworkException(this)
            else -> NetworkException(throwable = this, errorBody = null, parsedError = null)
        }

    companion object {
        private fun ResponseBody?.getCopyAndString(): Pair<ResponseBody?, String?> {
            if (this == null) return null to null

            val source = source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()

            return buffer.asResponseBody(contentType(), contentLength()) to string()
        }

        private fun <Error> Converter<ResponseBody, Error?>.convertSafely(responseBody: ResponseBody): Any? =
            try {
                convert(responseBody)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                null
            }
    }

    class Factory(private val networkExceptionConverter: NetworkExceptionConverter) : ErrorResponseToExceptionConverter.Factory {

        override fun <Data, Error> create(type: Type, errorClass: Class<out Any>?, converter: Converter<ResponseBody, Error?>): ErrorResponseToExceptionConverter<Data> =
            NetworkExceptionToErrorResponseConverterAdapter(converter, networkExceptionConverter)

    }
}