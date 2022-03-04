package com.halcyonmobile.retrofit.errorparsing

import com.halcyonmobile.errorparsing2.ErrorWrappingAndParserCallAdapterFactory
import com.halcyonmobile.errorparsing2.ParsedError
import com.halcyonmobile.errorparsing2.ParsedErrorToExceptionConverter
import com.halcyonmobile.errorparsing2.add
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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
class ParsedErrorToExceptionConverterErrorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: Service

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory(ParsedErrorToExceptionConverter.DelegateFactory().add(PE2()).add(PE())))
            .baseUrl(mockWebServer.url("something/").toString())
            .build()
            .create(Service::class.java)
    }

    class PE : ParsedErrorToExceptionConverter<GeneralError>{
        override fun convert(model: GeneralError?): RuntimeException =
            IndexOutOfBoundsException()

        override fun convert(throwable: Throwable): RuntimeException {
            throw RuntimeException()
        }

    }
    class PE2 : ParsedErrorToExceptionConverter<GeneralError2>{
        override fun convert(model: GeneralError2?): RuntimeException =
            IllegalArgumentException()

        override fun convert(throwable: Throwable): RuntimeException {
            throw RuntimeException()
        }

    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun GIVEN_request_annotated_with_parsed_error_WHEN_the_requests_fails_THEN_the_error_is_parsed_properly() =
        runBlocking<Unit> {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.exceptionParsing()
            } catch (networkException: IndexOutOfBoundsException) {
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_annotated_with_parsed_error_WHEN_the_requests_fails_THEN_the_error_is_parsed_properly2() =
        runBlocking<Unit> {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.exceptionParsing2()
            } catch (networkException: IllegalArgumentException) {
                return@runBlocking
            }

            assertTrue("The request didn't thrown an exception", false)
        }


    interface Service {
        @ParsedError(value = GeneralError::class)
        @GET("alma")
        suspend fun exceptionParsing(): Unit?

        @ParsedError(value = GeneralError2::class)
        @GET("alma")
        suspend fun exceptionParsing2(): Unit?
    }

    @JsonClass(generateAdapter = true)
    data class Model constructor(@field:Json(name = "alma") val foo: String)

    @JsonClass(generateAdapter = true)
    data class GeneralError2(
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
}