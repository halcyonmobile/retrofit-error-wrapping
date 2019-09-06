package com.halcyonmobile.errorparsing

import okhttp3.ResponseBody
import retrofit2.*
import java.util.concurrent.CancellationException

/**
 * A wrapper class around a [Call] which actually catches exceptions and wraps them into
 * [NetworkException] and does the errorBody parsing if necessary.
 */
class ErrorWrappingAndParsingCall<T>(
    private val delegate: Call<T>,
    private val converter: Converter<ResponseBody, Any?>,
    private val networkExceptionConverter: NetworkExceptionConverter
) : DelegateCall<T>(delegate) {
    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) =
                callback.onFailure(call, t.mapToNetworkExceptionIfNeeded())

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
            throw error.mapToNetworkExceptionIfNeeded()
        }
        return mapResponseErrors(response)
    }

    private fun Throwable.mapToNetworkExceptionIfNeeded(): Throwable =
        takeIf { it is CancellationException } ?: networkExceptionConverter.convert(NetworkException(this, null, null))

    @Throws(Throwable::class)
    private fun mapResponseErrors(response: Response<T>): Response<T> {
        if (response.isSuccessful) {
            return response
        } else {
            val (errorBodyForParsing, errorBodyAsString) = response.errorBody().getCopyAndString()
            throw networkExceptionConverter.convert(
                NetworkException(
                HttpException(response),
                errorBodyForParsing?.let(::convert),
                errorBodyAsString
            ))
        }
    }

    private fun ResponseBody?.getCopyAndString() : Pair<ResponseBody?, String?>{
        if (this == null) return null to null

        val source = source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer.clone()

        return ResponseBody.create(contentType(), contentLength(), buffer) to string()
    }

    private fun convert(responseBody: ResponseBody): Any? =
        try {
            converter.convert(responseBody)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            null
        }

    override fun clone(): Call<T> = ErrorWrappingAndParsingCall(delegate.clone(), converter, networkExceptionConverter)
}