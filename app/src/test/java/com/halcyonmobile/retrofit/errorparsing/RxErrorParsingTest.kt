package com.halcyonmobile.retrofit.errorparsing

import com.halcyonmobile.errorparsing2.*
import com.squareup.moshi.Moshi
import io.reactivex.Observable
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
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.CountDownLatch

/**
 * Purpose
 * <p>
 * Description
 * <p/>
 * Notes:
 * @author (OPTIONAL! Use only if the code is complex, otherwise delete this line.)
 */
@Suppress("TestFunctionName")
class RxErrorParsingTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: Service

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(CallAdapterFactoryWrapper(RxJava2CallAdapterFactory.create()))
            .baseUrl(mockWebServer.url("something/").toString())
            .build()
            .create(Service::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun GIVEN_request_annotated_with_parsed_error_WHEN_the_requests_fails_THEN_the_error_is_parsed_properly() {
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
                var throwable: Throwable? = null
                val latch = CountDownLatch(1)
                service.exceptionParsing().singleOrError().subscribe { _, actualError ->
                    throwable = actualError
                    latch.countDown()
                }
                latch.await()
                throwable?.let { throw it }
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
                return
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_annotated_with_wrap_int_network_WHEN_the_Request_fails_THEN_the_exception_is_wrapped(){
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                var throwable: Throwable? = null
                val latch = CountDownLatch(1)
                service.networkErrorWrapping().singleOrError().subscribe { _, actualError ->
                    throwable = actualError
                    latch.countDown()
                }
                latch.await()
                throwable?.let { throw it }
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
                return
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    @Test
    fun GIVEN_request_without_annotated_WHEN_the_Request_fails_THEN_the_original_exception_is_thrown() {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                var throwable: Throwable? = null
                val latch = CountDownLatch(1)
                service.withoutAnnotation().singleOrError().subscribe { _, actualError ->
                    throwable = actualError
                    latch.countDown()
                }
                latch.await()
                throwable?.let { throw it }
            } catch (httpException: HttpException) {
                return
            }

            assertTrue("The request didn't thrown an exception", false)
        }

    interface Service {
        @ParsedError(value = GeneralError::class)
        @GET("alma")
        fun exceptionParsing(): Observable<Unit?>

        @WrapIntoNetworkException
        @GET("alma")
        fun networkErrorWrapping(): Observable<Unit?>

        @GET("alma")
        fun withoutAnnotation(): Observable<Unit?>
    }
}