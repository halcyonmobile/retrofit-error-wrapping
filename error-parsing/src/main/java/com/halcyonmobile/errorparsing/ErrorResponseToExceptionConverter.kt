package com.halcyonmobile.errorparsing

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