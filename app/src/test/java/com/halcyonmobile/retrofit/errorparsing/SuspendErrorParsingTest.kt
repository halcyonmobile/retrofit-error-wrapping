package com.halcyonmobile.retrofit.errorparsing

import com.halcyonmobile.errorparsing2.ErrorWrappingAndParserCallAdapterFactory
import com.halcyonmobile.errorparsing2.NetworkException
import com.halcyonmobile.errorparsing2.ParsedError
import com.halcyonmobile.errorparsing2.WrapIntoNetworkException
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * Purpose
 * <p>
 * Description
 * <p/>
 * Notes:
 * @author (OPTIONAL! Use only if the code is complex, otherwise delete this line.)
 */
@Suppress("TestFunctionName")
class SuspendErrorParsingTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: Service

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory())
            .baseUrl(mockWebServer.url("something/").toString())
            .build()
            .create(Service::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun GIVEN_request_annotated_with_parsed_error_WHEN_the_requests_fails_THEN_the_error_is_parsed_properly() =
        runBlocking<Unit> {
            val expectedError = GeneralError(
                causes = listOf(
                    GeneralError.ErrorCause(
                        code = "invalid_paremeter_username",
                        developerMessage = "The username parameter is not valid (too short)",
                        errorMessage = "The username is too short, please provide something longer"
                    )
                ),
                timestamp = "1231",
                status = 400,
                message = "Bad Request"
            )
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.exceptionParsing()
            } catch (networkException: NetworkException) {
                assertEquals(expectedError, networkException.parsedError)
                JSONAssert.assertEquals(
                    readJsonResourceFileToString("simple_error.json"),
                    networkException.errorBody,
                    JSONCompareMode.LENIENT
                )
                assertTrue(networkException.cause is HttpException)
                JSONAssert.assertEquals(
                    readJsonResourceFileToString("simple_error.json"),
                    (networkException.cause as HttpException).response()?.errorBody()?.string(),
                    JSONCompareMode.LENIENT
                )
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_annotated_with_wrap_int_network_WHEN_the_Request_fails_THEN_the_exception_is_wrapped() =
        runBlocking<Unit> {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.networkErrorWrapping()
            } catch (networkException: NetworkException) {
                assertEquals(null, networkException.parsedError)
                JSONAssert.assertEquals(
                    readJsonResourceFileToString("simple_error.json"),
                    networkException.errorBody,
                    JSONCompareMode.LENIENT
                )
                assertTrue(networkException.cause is HttpException)
                JSONAssert.assertEquals(
                    readJsonResourceFileToString("simple_error.json"),
                    (networkException.cause as HttpException).response()?.errorBody()?.string(),
                    JSONCompareMode.LENIENT
                )
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_without_annotated_WHEN_the_Request_fails_THEN_the_original_exception_is_thrown() =
        runBlocking<Unit> {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.withoutAnnotation()
            } catch (httpException: HttpException) {
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_annotated_with_wrap_into_network_WHEN_the_parsing_fails_THEN_the_exception_is_wrapped() =
        runBlocking {
            mockWebServer.enqueueRequest(200, "")

            try {
                service.modelParsing()
            } catch (networkException: NetworkException) {
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_annotated_with_wrap_into_network_and_returning_response_body_WHEN_the_request_fails_THEN_the_exception_is_wrapped() =
        runBlocking {
            mockWebServer.enqueueRequest(400, "")

            try {
                service.responseBody()
            } catch (networkException: NetworkException) {
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }


    interface Service {
        @ParsedError(value = GeneralError::class)
        @GET("alma")
        suspend fun exceptionParsing(): Unit?

        @WrapIntoNetworkException
        @GET("alma")
        suspend fun networkErrorWrapping(): Unit?

        @GET("alma")
        suspend fun withoutAnnotation(): Unit?

        @WrapIntoNetworkException
        @GET("alma")
        suspend fun modelParsing() : Model

        @WrapIntoNetworkException
        @GET("alma")
        suspend fun responseBody() : ResponseBody
    }

    @JsonClass(generateAdapter = true)
    data class Model constructor(@field:Json(name = "alma") val foo: String)
}