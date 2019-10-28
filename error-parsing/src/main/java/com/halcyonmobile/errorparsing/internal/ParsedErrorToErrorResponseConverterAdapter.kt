package com.halcyonmobile.errorparsing.internal

import com.halcyonmobile.errorparsing.ErrorResponseToExceptionConverter
import com.halcyonmobile.errorparsing.ParsedErrorToExceptionConverter
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.lang.reflect.Type

/**
 * Adapter which adapts a [ParsedErrorToExceptionConverter] into an [ErrorResponseToExceptionConverter] so it can be used in [ErrorWrappingAndParsingCall]
 */
internal class ParsedErrorToErrorResponseConverterAdapter<T, ParsedError>(
    private val parsedErrorToExceptionConverter: ParsedErrorToExceptionConverter<ParsedError>,
    private val converter: Converter<ResponseBody, ParsedError?>
) : ErrorResponseToExceptionConverter<T> {

    override fun convert(response: Response<T>): RuntimeException =
        parsedErrorToExceptionConverter.convert(response.errorBody()?.let(converter::convert))

    override fun convert(throwable: Throwable): RuntimeException =
        parsedErrorToExceptionConverter.convert(throwable)


    class Factory(private val parsedErrorToExceptionConverterFactory: ParsedErrorToExceptionConverter.Factory) : ErrorResponseToExceptionConverter.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <Data, Error> create(type: Type, errorClass: Class<out Any>?, converter: Converter<ResponseBody, Error?>): ErrorResponseToExceptionConverter<Data> =
            ParsedErrorToErrorResponseConverterAdapter(
                parsedErrorToExceptionConverter = parsedErrorToExceptionConverterFactory.create(errorClass as Class<out Error>?) as ParsedErrorToExceptionConverter<Error>,
                converter = converter
            )

    }
}