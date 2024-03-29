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

import com.halcyonmobile.errorparsing2.loggers.ErrorParsingFailureLogger
import okhttp3.ResponseBody
import retrofit2.Converter

class ErrorParsingFailureLoggingConverter(
    private val errorParsingFailureLogger: ErrorParsingFailureLogger,
    private val converter: Converter<ResponseBody, Any?>
) : Converter<ResponseBody, Any?> {
    override fun convert(value: ResponseBody): Any? {
        try {
            return converter.convert(value)
        } catch (throwable: Throwable) {
            errorParsingFailureLogger.log(throwable)
            throw throwable
        }
    }

}