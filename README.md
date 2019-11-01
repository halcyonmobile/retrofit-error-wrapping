# A retrofit call adapter factory which wraps every exception to a defined one

## Purpose

The purpose of this library is to ease general exception handling caught from retrofit.
Most of our projects has a piece of code which wraps any exception thrown by retrofit into a custom defined one, so why don't just have a lib which does this for us?

## Setup

latest version is 0.1.0.3

### Add artifactory to your dependencies
 - in your top-level build.gradle add the following setup to access halcyon libraries :
```groovy
allprojects {
    repositories {
        /*...*/
        // For internal HalcyonMobile libraries
        maven {
            url "https://artifactory.build.halcyonmobile.com/artifactory/libs-release-local/"
                credentials {
                    username = "${artifactory_username}"
                    password = "${artifactory_password}"
                }
        }
    }
}
```

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

class NetworkExceptionToCustomException : NetworkExceptionConverter{
    override fun convert(networkException: NetworkException): Throwable = CustomException()
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
