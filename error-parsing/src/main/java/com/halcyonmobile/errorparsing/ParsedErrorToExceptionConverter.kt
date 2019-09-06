package com.halcyonmobile.errorparsing

/**
 * A Converter which maps a parsed error into a custom [RuntimeException]
 */
interface ParsedErrorToExceptionConverter<ParsedError> {

    /**
     * Converts the [model] parsed error into a custom [RuntimeException]
     */
    fun convert(model: ParsedError?): RuntimeException

    /**
     * Maps an error found while waiting for the response.
     */
    fun convert(throwable: Throwable): RuntimeException

    /**
     * Factory which returns the proper [ParsedErrorToExceptionConverter] by the type of the error which can be parsed.
     */
    interface Factory {
        fun <ParsedError> create(errorClass: Class<out ParsedError>?): ParsedErrorToExceptionConverter<*>
    }

    /**
     * Helper factory which just maps for each type a [ParsedErrorToExceptionConverter]
     */
    class DelegateFactory(parsedErrorToExceptionConvertersByTypes: Map<in Class<*>, ParsedErrorToExceptionConverter<out Any>> = mutableMapOf()) : Factory{
        private val parsedErrorToExceptionConvertersByTypes = parsedErrorToExceptionConvertersByTypes.toMutableMap()


        override fun <ParsedError> create(errorClass: Class<out ParsedError>?): ParsedErrorToExceptionConverter<*> =
            parsedErrorToExceptionConvertersByTypes[errorClass] ?: throw IllegalStateException("Didn't find converter for $errorClass")

        fun <Error: Any> add(error: Class<Error>, converter: ParsedErrorToExceptionConverter<Error>): DelegateFactory = apply {
            parsedErrorToExceptionConvertersByTypes[error] = converter
        }

    }
}

/**
 * Helper function to add [ParsedErrorToExceptionConverter] to the [ParsedErrorToExceptionConverter.DelegateFactory]
 */
inline fun <reified Error: Any> ParsedErrorToExceptionConverter.DelegateFactory.add(converter: ParsedErrorToExceptionConverter<Error>): ParsedErrorToExceptionConverter.DelegateFactory =
    add(Error::class.java, converter)