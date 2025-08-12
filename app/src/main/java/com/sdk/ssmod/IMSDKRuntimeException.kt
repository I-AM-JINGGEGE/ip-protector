package com.sdk.ssmod

abstract class IMSDKRuntimeException : RuntimeException() {
    abstract val errorCode: Int
}

class ServerUnreachableException : IMSDKRuntimeException() {
    override val errorCode: Int = -1
    override val message: String = "There isn't any server that can be reached."
}

class ServerZoneNotFoundException(serverZoneId: String) : IMSDKRuntimeException() {
    override val errorCode: Int = -2
    override val message: String = "The server zone ID '$serverZoneId' doesn't exist."
}

class CountryOfServerNotFoundException(country: String) : IMSDKRuntimeException() {
    override val errorCode: Int = -3
    override val message: String = "Country '$country' not found."
}
