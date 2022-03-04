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

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.lang.reflect.Type

/**
 * An Error Converter which maps a retrofit error response into an exception.
 */
interface ErrorResponseToExceptionConverter<T>{

    /**
     * Maps the [response] into a custom [RuntimeException].
     *
     * Note: this is only called if the [response] is not successful ([Response.isSuccessful] is false)
     */
    fun convert(response:  Response<T>) : RuntimeException

    /**
     * Maps an error found while waiting for the response.
     */
    fun convert(throwable: Throwable) : RuntimeException

    interface Factory{

        fun <Data, Error> create(type: Type, errorClass: Class<out Any>?, converter: Converter<ResponseBody, Error?>) : ErrorResponseToExceptionConverter<Data>
    }
}