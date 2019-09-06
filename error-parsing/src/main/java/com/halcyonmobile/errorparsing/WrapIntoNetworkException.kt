package com.halcyonmobile.errorparsing

/**
 * Annotation to mark [ErrorWrappingAndParserCallAdapterFactory] that the method should wrap it's
 * errors into [NetworkException] while not parsing the errorBody into it.
 */
annotation class WrapIntoNetworkException