package com.halcyonmobile.errorparsing

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Call adapter factory which can be used to wrap any error thrown by the request into a
 * [NetworkException] if the request is annotated with [WrapIntoNetworkException] or [ParsedError].
 *
 * If [ParsedError] annotation is used then the errorBody will also be parsed into the exception.
 * For the parsing process the [Retrofit.converterFactories] are used.
 */
class ErrorWrappingAndParserCallAdapterFactory(
    private val networkExceptionConverter: NetworkExceptionConverter = DummyNetworkExceptionConverter(),
    private val workWithoutAnnotation: Boolean = false
) : CallAdapter.Factory() {

    @Suppress("UNCHECKED_CAST")
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val converter = annotations
            .asSequence()
            .filterIsInstance<ParsedError>()
            .map(ParsedError::value)
            .map { it.java }
            .firstOrNull()
            ?.let {
                retrofit.responseBodyConverter<Any?>(it, annotations)
            }
        if (converter == null && annotations.none { it is WrapIntoNetworkException } && !workWithoutAnnotation) {
            return null
        }

        val rawType = getParameterUpperBound(0, returnType as ParameterizedType)
        return Adapter<Any>(rawType, converter ?: NullConverter(), networkExceptionConverter)
    }

    class Adapter<T>(
        private val rawType: Type,
        private val converter: Converter<ResponseBody, Any?>,
        private val networkExceptionConverter: NetworkExceptionConverter
    ) : CallAdapter<T, Call<T>> {

        override fun adapt(call: Call<T>): Call<T> = ErrorWrappingAndParsingCall(call, converter, networkExceptionConverter)

        override fun responseType(): Type = rawType
    }

    /**
     * Fake converter which always returns null.
     */
    class NullConverter : Converter<ResponseBody, Any?>{
        override fun convert(value: ResponseBody): Any? = null
    }
}