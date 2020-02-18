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

import okhttp3.Request
import retrofit2.Call

/**
 * Simple base class to simplify the implementation of a [Call] which wraps another [Call] object.
 */
abstract class DelegateCall<T>(private val call: Call<T>) : Call<T> {

    override fun isExecuted(): Boolean = call.isExecuted

    override fun isCanceled(): Boolean = call.isCanceled

    override fun cancel() = call.cancel()

    override fun request(): Request = call.request()
}