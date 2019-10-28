package com.halcyonmobile.errorparsing

import com.halcyonmobile.errorparsing.internal.ErrorWrappingAndParsingCall
import com.halcyonmobile.errorparsing.internal.NetworkExceptionToErrorResponseConverterAdapter
import com.halcyonmobile.errorparsing.internal.ParsedErrorToErrorResponseConverterAdapter
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
    private val errorResponseToExceptionConverterFactory: ErrorResponseToExceptionConverter.Factory,
    private val workWithoutAnnotation: Boolean = false
) : CallAdapter.Factory() {

    constructor(networkExceptionConverter: NetworkExceptionConverter = DummyNetworkExceptionConverter(), workWithoutAnnotation: Boolean = false) : this(
        errorResponseToExceptionConverterFactory = NetworkExceptionToErrorResponseConverterAdapter.Factory(networkExceptionConverter),
        workWithoutAnnotation = workWithoutAnnotation
    )

    constructor(parsedErrorToExceptionConverterFactory: ParsedErrorToExceptionConverter.Factory, workWithoutAnnotation: Boolean = false) : this(
        errorResponseToExceptionConverterFactory = ParsedErrorToErrorResponseConverterAdapter.Factory(parsedErrorToExceptionConverterFactory),
        workWithoutAnnotation = workWithoutAnnotation
    )

    @Suppress("UNCHECKED_CAST")
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val errorClass = annotations
            .asSequence()
            .filterIsInstance<ParsedError>()
            .map(ParsedError::value)
            .map { it.java }
            .firstOrNull()
        val converter = errorClass?.let { retrofit.responseBodyConverter<Any?>(it, annotations) }

        if (converter == null && annotations.none { it is WrapIntoNetworkException } && !workWithoutAnnotation) {
            return null
        }

        val rawType = getParameterUpperBound(0, returnType as ParameterizedType)
        return Adapter<Any>(rawType, errorResponseToExceptionConverterFactory.create(rawType, errorClass, converter ?: NullConverter()))
    }

    class Adapter<T>(
        private val rawType: Type,
        private val errorResponseToExceptionConverter: ErrorResponseToExceptionConverter<T>
    ) : CallAdapter<T, Call<T>> {

        override fun adapt(call: Call<T>): Call<T> = ErrorWrappingAndParsingCall(call, errorResponseToExceptionConverter)

        override fun responseType(): Type = rawType
    }

    /**
     * Fake converter which always returns null.
     */
    class NullConverter : Converter<ResponseBody, Any?> {
        override fun convert(value: ResponseBody): Any? = null
    }
}