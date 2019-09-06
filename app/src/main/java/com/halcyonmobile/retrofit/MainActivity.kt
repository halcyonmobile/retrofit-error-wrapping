package com.halcyonmobile.retrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.halcyonmobile.errorparsing.ErrorWrappingAndParserCallAdapterFactory
import com.halcyonmobile.errorparsing.NetworkException
import com.halcyonmobile.errorparsing.WrapIntoNetworkException
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val service = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory())
            .baseUrl("https://www.googlex.com/")
            .build()
            .create(Service::class.java)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                service.networkErrorWrapping()
            } catch (networkException: NetworkException){
                networkException.printStackTrace()
                System.err.println("error caught")
            }
        }
    }

    interface Service {

        @WrapIntoNetworkException
        @GET("alma")
        suspend fun networkErrorWrapping(): Unit?
    }
}