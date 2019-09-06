package com.halcyonmobile.errorparsing

import kotlin.reflect.KClass

/**
 * Annotation to mark [ErrorWrappingAndParserCallAdapterFactory] that the method should wrap it's
 * errors into [NetworkException] and parse the errorBody into it.
 */
annotation class ParsedError(val value: KClass<*>)