package utils

import java.nio.ByteBuffer

fun ByteArray.isHttpRequest(): Boolean {
    return String(this).take(HTTP_REQUEST.length) == HTTP_REQUEST
}

fun ByteBuffer.isHttpRequest(): Boolean {
    return String(array()).take(HTTP_REQUEST.length) == HTTP_REQUEST
}

fun ByteArray.checkRequest() {
    require(size == REQUEST_SIZE)
    require(String(this).split("$NEW_LINE$NEW_LINE")[1] == String(BODY))
}

fun ByteBuffer.checkRequest() {
    require(limit() == REQUEST_SIZE)
    require(String(array()).split("$NEW_LINE$NEW_LINE")[1] == String(BODY))
}