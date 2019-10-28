package com.halcyonmobile.errorparsing.internal

import com.halcyonmobile.errorparsing.ErrorResponseToExceptionConverter
import com.halcyonmobile.errorparsing.NetworkException
import com.halcyonmobile.errorparsing.NetworkExceptionConverter
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Type

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

    override fun convert(throwable: Throwable): RuntimeException =
        networkExceptionConverter.convert(throwable as? NetworkException ?: NetworkException(throwable = throwable, errorBody = null, parsedError = null))

    companion object {
        private fun ResponseBody?.getCopyAndString(): Pair<ResponseBody?, String?> {
            if (this == null) return null to null

            val source = source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()

            return ResponseBody.create(contentType(), contentLength(), buffer) to string()
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