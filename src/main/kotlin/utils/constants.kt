package utils

import java.nio.ByteBuffer

const val DATA_ARRAY_SIZE = 3 * 1024 // 3 KB
val DATA_ARRAY = ByteArray(DATA_ARRAY_SIZE)
val DATA_BUFFER: ByteBuffer = ByteBuffer.wrap(DATA_ARRAY)

const val SERVER_ADDRESS = "localhost"
const val SERVER_PORT = 12345

const val COUNT_OF_ITERATIONS = 1

const val COUNT_OF_CLIENTS = 1000

const val FILE_NAME = "results/1_non-blocking_server_${COUNT_OF_CLIENTS}_blocking_clients.txt"