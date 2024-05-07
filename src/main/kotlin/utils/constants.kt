package utils

import java.nio.ByteBuffer
import kotlin.properties.Delegates

val SERVER_ADDRESS = System.getenv("HOST_ADDRESS") ?: "localhost"
const val SERVER_PORT = 12345
const val SERVER_BACKLOG = 1000000
const val COUNT_OF_ITERATIONS = 1
const val COUNT_OF_CLIENTS = 1000

var BODY_SIZE by Delegates.notNull<Int>()

val BODY by lazy { ByteArray(BODY_SIZE) { '0'.code.toByte() } }

const val NEW_LINE = "\r\n"
const val HTTP_REQUEST = "POST / HTTP/1.1"
private val REQUEST_HEADERS by lazy {
    HTTP_REQUEST + NEW_LINE +
            "Host: $SERVER_ADDRESS:$SERVER_PORT" + NEW_LINE +
            "Content-Length: $BODY_SIZE" + NEW_LINE +
            NEW_LINE
}

val REQUEST by lazy { REQUEST_HEADERS.toByteArray() + BODY }
val REQUEST_SIZE by lazy { REQUEST.size }

private const val HTTP_RESPONSE = "HTTP/1.1 200 OK"
private val RESPONSE_HEADERS by lazy {
    HTTP_RESPONSE + NEW_LINE +
            "Content-Length: $BODY_SIZE" + NEW_LINE +
            NEW_LINE
}

val RESPONSE by lazy { RESPONSE_HEADERS.toByteArray() + BODY }
val RESPONSE_SIZE by lazy { RESPONSE.size }
val RESPONSE_BUFFER: ByteBuffer by lazy { ByteBuffer.wrap(RESPONSE) }

const val DEBUG = false
