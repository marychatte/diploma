package utils

import java.nio.ByteBuffer

const val NEW_LINE = "\r\n"
const val HTTP_REQUEST = "POST / HTTP/1.1"

const val BODY_SIZE = 3 * 1024 // 3 KB
val BODY = ByteArray(BODY_SIZE) { '0'.code.toByte() }

const val SERVER_ADDRESS = "localhost"
const val SERVER_PORT = 12345
private const val REQUEST_HEADERS = HTTP_REQUEST + NEW_LINE +
        "Host: $SERVER_ADDRESS:$SERVER_PORT" + NEW_LINE +
        "Content-Length: $BODY_SIZE" + NEW_LINE +
        NEW_LINE
val REQUEST = REQUEST_HEADERS.toByteArray() + BODY
val REQUEST_SIZE = REQUEST.size
val REQUEST_BUFFER: ByteBuffer = ByteBuffer.wrap(REQUEST)

const val HTTP_RESPONSE = "HTTP/1.1 200 OK"
private const val RESPONSE_HEADERS = HTTP_RESPONSE + NEW_LINE +
        "Content-Length: $BODY_SIZE" + NEW_LINE +
        NEW_LINE
val RESPONSE = RESPONSE_HEADERS.toByteArray() + BODY
val RESPONSE_SIZE = RESPONSE.size
val RESPONSE_BUFFER: ByteBuffer = ByteBuffer.wrap(RESPONSE)

const val SERVER_BACKLOG = 32760

const val COUNT_OF_ITERATIONS = 1

const val COUNT_OF_CLIENTS = 1000

const val DEBUG = false
