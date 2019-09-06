package com.halcyonmobile.retrofit.errorparsing

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

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
fun Any.readJsonResourceFileToString(fileName: String): String {
    val path = this::class.java.classLoader!!.getResource(fileName).toURI().path
    return Files.lines(Paths.get(path)).collect(Collectors.joining())
}