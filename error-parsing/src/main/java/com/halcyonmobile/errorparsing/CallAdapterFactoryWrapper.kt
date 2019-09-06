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