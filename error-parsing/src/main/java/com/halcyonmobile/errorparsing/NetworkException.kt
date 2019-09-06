package com.halcyonmobile.errorparsing

/**
 * General Network Exception, it't contains the errorBody, if there were any in [errorBody] as string
 * and a parsed errorBody in [parsedError] if it could be parsed.
 */
open class NetworkException constructor(throwable: Throwable?, val parsedError: Any?, val errorBody: String?) : RuntimeException(throwable)