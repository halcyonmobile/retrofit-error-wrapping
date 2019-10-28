package com.halcyonmobile.errorparsing

/**
 * A simple converter which can be used to throw your own exception instead of [NetworkException]
 */
interface NetworkExceptionConverter {

    fun convert(networkException: NetworkException): RuntimeException
}