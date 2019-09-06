package com.halcyonmobile.retrofit.errorparsing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Purpose
 * <p>
 * Description
 * <p/>
 * Notes:
 * @author (OPTIONAL! Use only if the code is complex, otherwise delete this line.)
 */
@JsonClass(generateAdapter = true)
data class GeneralError(
    @field:Json(name = "errors") val causes: List<ErrorCause>,
    @field:Json(name = "message") val message: String,
    @field:Json(name = "status") val status: Int,
    @field:Json(name = "timestamp") val timestamp: String
) {
    @JsonClass(generateAdapter = true)
    data class ErrorCause(
        @field:Json(name = "code") val code: String?,
        @field:Json(name = "developerMessage") val developerMessage: String?,
        @field:Json(name = "errorMessage") val errorMessage: String?
    )
}