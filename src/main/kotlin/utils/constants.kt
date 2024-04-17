package utils

import java.nio.ByteBuffer

const val NEW_LINE = "\r\n"
const val HTTP_REQUEST = "POST / HTTP/1.1"

const val BODY_SIZE = 3 * 1024 // 3 KB
val BODY = ByteArray(BODY_SIZE) { '0'.code.toByte() }

private const val REQUEST_HEADERS = HTTP_REQUEST + NEW_LINE +
        "Host: localhost:12345" + NEW_LINE +
        "Content-Length: 3072" + NEW_LINE +
        NEW_LINE
val REQUEST = REQUEST_HEADERS.toByteArray() + BODY
val REQUEST_SIZE = REQUEST.size
val REQUEST_BUFFER: ByteBuffer = ByteBuffer.wrap(REQUEST)


private const val RESPONSE_HEADERS = "HTTP/1.1 200 OK" + NEW_LINE +
        "Content-Length: $BODY_SIZE" + NEW_LINE +
        NEW_LINE
val RESPONSE = RESPONSE_HEADERS.toByteArray() + BODY
val RESPONSE_BUFFER: ByteBuffer = ByteBuffer.wrap(RESPONSE)
val RESPONSE_SIZE = RESPONSE.size

const val SERVER_ADDRESS = "localhost"
const val SERVER_PORT = 12345
const val SERVER_BACKLOG = 32760

const val COUNT_OF_ITERATIONS = 1

const val COUNT_OF_CLIENTS = 1000

const val DEBUG = false
