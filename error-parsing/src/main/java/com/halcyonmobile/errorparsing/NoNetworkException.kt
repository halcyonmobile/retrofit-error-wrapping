package com.halcyonmobile.errorparsing

/**
 * Exception representing when there is no internet connection and the request failed because of that.
 *
 * Note: this might be thrown even if the user has internet connection, but the device couldn't connect to the server via the url, meaning it couldn't find the server through DNS.
 */
class NoNetworkException(throwable: Throwable?) : NetworkException(throwable, null, null)