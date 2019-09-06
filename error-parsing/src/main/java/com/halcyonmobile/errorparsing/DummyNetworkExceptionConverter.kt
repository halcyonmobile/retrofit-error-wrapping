package com.halcyonmobile.errorparsing

import java.lang.RuntimeException

/**
 * Purpose
 * <p>
 * Description
 * <p/>
 * Notes:
 * @author (OPTIONAL! Use only if the code is complex, otherwise delete this line.)
 */
class DummyNetworkExceptionConverter : NetworkExceptionConverter{
    override fun convert(networkException: NetworkException): RuntimeException = networkException
}