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

package com.halcyonmobile.errorparsing

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] which can be used to combine another [CallAdapter.Factory] with [ErrorWrappingAndParserCallAdapterFactory]
 */
class CallAdapterFactoryWrapper(
    private val callAdapterFactory: CallAdapter.Factory,
    private val errorWrappingAndParserCallAdapterFactory: ErrorWrappingAndParserCallAdapterFactory = ErrorWrappingAndParserCallAdapterFactory()
) : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val callAdapter = callAdapterFactory.get(returnType, annotations, retrofit)
        val errorWrappingAndParserCallAdapter = errorWrappingAndParserCallAdapterFactory.get(returnType, annotations, retrofit) ?: return callAdapter

        @Suppress("UNCHECKED_CAST")
        return CallAdapterWrapper(callAdapter as CallAdapter<Any?, *>, errorWrappingAndParserCallAdapter as CallAdapter<Any?, *>)
    }

    class CallAdapterWrapper(
        private val callAdapter: CallAdapter<Any?, *>,
        private val errorWrappingAndParserCallAdapter: CallAdapter<Any?, *>
    ) : CallAdapter<Any?, Any?> {

        @Suppress("UNCHECKED_CAST")
        override fun adapt(call: Call<Any?>): Any =
            callAdapter.adapt(errorWrappingAndParserCallAdapter.adapt(call) as Call<Any?>)

        override fun responseType(): Type = callAdapter.responseType()

    }
}