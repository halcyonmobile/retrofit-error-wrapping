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