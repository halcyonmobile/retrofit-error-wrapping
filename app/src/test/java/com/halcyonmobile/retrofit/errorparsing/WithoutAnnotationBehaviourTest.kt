package com.halcyonmobile.retrofit.errorparsing

import com.halcyonmobile.errorparsing.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

@Suppress("TestFunctionName")
class WithoutAnnotationBehaviourTest{

    private lateinit var mockWebServer: MockWebServer
    private lateinit var service: Service

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory(workWithoutAnnotation = true))
            .baseUrl(mockWebServer.url("something/").toString())
            .build()
            .create(Service::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun GIVEN_request_annotated_with_wrap_int_network_WHEN_the_Request_fails_THEN_the_exception_is_wrapped() =
        runBlocking<Unit> {
            mockWebServer.enqueueRequest(400, readJsonResourceFileToString("simple_error.json"))

            try {
                service.withoutAnnotation()
            } catch (networkException: NetworkException) {
                return@runBlocking
            }

            Assert.assertTrue("The request didn't thrown an exception", false)
        }

    interface Service {

        @GET("alma")
        suspend fun withoutAnnotation(): Unit?
    }
}