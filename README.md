# A retrofit call adapter factory which wraps every exception to a defined one

## Purpose

The purpose of this library is to ease general exception handling caught from retrofit.

Most of our projects has a piece of code which wraps any exception thrown by retrofit into a custom defined one, so why don't just have a lib which does this for us?

### I only want to map my api response exceptions into a custom Exception, what should I do?

In this case you may not need this library, which wraps all exceptions, such as network errors, parsing errors etc.

For this you can implement an interceptor, here is an example how can you do that

```kotlin
class NetworkErrorInterceptor(private val parser: Parser) : Interceptor {

    // not this is important because otherwise it might be wrapped into an UndeclaredThrowableException even with kotlin because of reflection proxy retrofit / okhttp uses
    @Throws(IOException::class, NetworkError::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request()) // this may throw IOException
        if (!response.isSuccessful) {
            try {
                val errorResponse = response.body?.source()?.let { errorResponseBody ->
                    parser.parse(errorResponseBody)
                }
                throw NetworkError(errorResponse)
            } catch (exception: IOException) {
                throw NetworkError(exception.message.orEmpty())
            }
        }
        return response
    }
}
```

## Setup

*Latest version:* ![Latest release](https://img.shields.io/github/v/release/halcyonmobile/retrofit-error-wrapping)

## Ensure you have the HalcyonMobile GitHub Packages as a repository

```gradle
// top level build.gradle
allprojects {
    repositories {
        // ...
        maven {
            url "https://maven.pkg.github.com/halcyonmobile/android-common-extensions"
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
  }
}
```

Note: you only need one maven declaration with "halcyonmobile/{specific}", every other package will be accessable.

### Add the dependency in your build.gradle

```groovy
implementation "com.halcyonmobile.retrofit-error-parsing:retrofit-error-parsing:latest-version"
```

### Add the Call adapter to your retrofit:
```kotlin

Retrofit.Builder()
    //...
    .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory())
    .build()
```

### Annotate your service function where you want the exception to be wrapped
```kotlin

interface Service {

    @WrapIntoNetworkException
    @GET("alma")
    suspend fun networkErrorWrapping(): Unit?
}
```

- Now this function will throw NetworkException instead.

## Configuration

### I don't want to annotate all of my service functions

then you should set the workWithoutAnnotation flag for the call adapter factory

```kotlin

Retrofit.Builder()
    //...
    .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory(workWithoutAnnotation = true))
    .build()
```

### I want to use my own exception type

then add a NetworkExceptionConverter to the call adapter factory

```kotlin

class CustomException: Throwable()

class NetworkExceptionToCustomException : NetworkExceptionConverter {
    override fun convert(networkException: NetworkException): RuntimeException = CustomException()
}

// ...
Retrofit.Builder()
    //...
    .addCallAdapterFactory(ErrorWrappingAndParserCallAdapterFactory(NetworkExceptionToCustomException()))
    .build()
```

### I want to parse the error body as json to some model

then use the @ParseError annotation instead

```kotlin

interface Service {
    @ParsedError(value = GeneralError::class)
    @GET("alma")
    suspend fun exceptionParsing(): Unit?
```

Now your NetworkException.parsedError will be a GeneralError if it could be parsed. If not then it will be simply null.
Note: you have to cast to your model type.
Note2: the errorBody is still contained in the NetworkException as string.

### I want to use this but I have another call adapter factory

then use CallAdapterFactoryWrapper such as

```kotlin

Retrofit.Builder()
            //...
            .addCallAdapterFactory(CallAdapterFactoryWrapper(RxJava2CallAdapterFactory.create()))
            .build()
```

Now you are free to use RxObservables while getting only NetworkExceptions in the onError.

### I want to log the exceptions if the error couldn't be parsed

For this you can implement the ErrorParsingFailureLogger and attach it to the CallAdapter

### I have to show a specific error for no internet, what should I do?

There is a specific NetworkException called NoNetworkException this is thrown when retrofit couldn't look up the server's address based on the url provided, meaning the url is either no longer valid or you simply can't connect to the DNS, you don't have network connection.
This should be sufficient for no-internet error handling in most cases.

### I want to use my own mapping from rawResponse / parsed error into an Exception, can i do that?

Yes you can, however in this case you are responsible for providing No-Internet exception

You can use the other constructors of the ErrorWrappingAndParserCallAdapterFactory. You can implement ErrorResponseToExceptionConverter.Factory and provide your own ErrorResponseToExceptionConverter for every type of response you receive.
You will get the raw response if it wasn't successful or the raw throwable if some exception occurred while processing the request.

In case of parsed model to Exception you can use ParsedErrorToExceptionConverter.Factory. You can provide a different ParsedErrorToExceptionConverter for each exception being parsed. You can also use DelegateFactory to add your ParsedErrorToExceptionConverter for each specific type.

<h1 id="license">License :page_facing_up:</h1>

Copyright (c) 2020 Halcyon Mobile.
> https://www.halcyonmobile.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
