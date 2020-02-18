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

package com.halcyonmobile.errorparsing.internal

import com.halcyonmobile.errorparsing.ErrorResponseToExceptionConverter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CancellationException

/**
 * A wrapper class around a [Call] which actually catches exceptions and wraps them into
 * [NetworkException] and does the errorBody parsing if necessary.
 */
internal class ErrorWrappingAndParsingCall<T>(
    private val delegate: Call<T>,
    private val errorResponseToExceptionConverter: ErrorResponseToExceptionConverter<T>
) : DelegateCall<T>(delegate) {
    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) =
                callback.onFailure(call, errorResponseToExceptionConverter.convertIfNeeded(t))

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val resultResponse = try {
                    mapResponseErrors(response)
                } catch (throwable: Throwable) {
                    callback.onFailure(call, throwable)
                    return
                }
                callback.onResponse(call, resultResponse)
            }
        })
    }

    override fun execute(): Response<T> {
        val response = try {
            delegate.execute()
        } catch (error: Throwable) {
            throw errorResponseToExceptionConverter.convertIfNeeded(error)
        }
        return mapResponseErrors(response)
    }

    @Throws(Throwable::class)
    private fun mapResponseErrors(response: Response<T>): Response<T> {
        if (response.isSuccessful) {
            return response
        } else {
            throw errorResponseToExceptionConverter.convert(response)
        }
    }

    private fun ErrorResponseToExceptionConverter<T>.convertIfNeeded(throwable: Throwable): Throwable =
        throwable.takeIf { it is CancellationException } ?: convert(throwable)

    override fun clone(): Call<T> = ErrorWrappingAndParsingCall(delegate.clone(), errorResponseToExceptionConverter)
}