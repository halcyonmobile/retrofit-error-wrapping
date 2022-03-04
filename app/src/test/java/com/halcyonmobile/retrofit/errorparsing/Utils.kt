package com.halcyonmobile.retrofit.errorparsing

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Enqueue a [MockResponse] with the given [bodyJson] as [MockResponse.body] and given [responseCode] as [MockResponse.setResponseCode]
 */
fun MockWebServer.enqueueRequest(responseCode: Int = 200, bodyJson: String) =
    enqueue(MockResponse().apply {
        setBody(bodyJson)
        setResponseCode(responseCode)
    })

/**
 * Reads content of the given [fileName] resource file into a String
 */
fun Any.readJsonResourceFileToString(fileName: String): String = try {
    BufferedReader(InputStreamReader(this.javaClass.classLoader?.getResourceAsStream(fileName)!!))
        .readLines().joinToString("\n")
} catch (nullPointerException: NullPointerException) {
    throw IllegalArgumentException("$fileName file not found!", nullPointerException)
}