package com.halcyonmobile.errorparsing

/**
 * Purpose
 * <p>
 * Description
 * <p/>
 * Notes:
 * @author (OPTIONAL! Use only if the code is complex, otherwise delete this line.)
 */
interface NetworkExceptionConverter{
    fun convert(networkException: NetworkException) : RuntimeException
}