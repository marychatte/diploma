package blocking

import utils.*
import java.io.InputStream
import java.net.ServerSocket

class BlockingServer {
    private val serverSocket = ServerSocket(SERVER_PORT, SERVER_BACKLOG)

    fun start() {
        while (true) {
            handleClient()
        }
    }

    fun stop() {
        serverSocket.close()
    }

    private fun handleClient() {
        val clientSocket = serverSocket.accept().apply {
            setTcpNoDelay(true)
        }
        Thread {
            try {
                while (true) {
                    val inputStream = clientSocket.getInputStream()
                    val receivedByteArray = read(inputStream)

                    if (!DEBUG) {
                        if (!receivedByteArray.isHttpRequest()) {
                            break
                        }
                    }

                    if (DEBUG) {
                        receivedByteArray.checkRequest()
                    }

                    val outputStream = clientSocket.getOutputStream()
                    outputStream.write(RESPONSE)
                    outputStream.flush()

                    if (DEBUG) {
                        break
                    }
                }
            } catch (_: Throwable) {
            } finally {
                clientSocket.close()
            }
        }.start()
    }

    private fun read(inputStream: InputStream): ByteArray {
        val receivedByteArray = ByteArray(REQUEST_SIZE)
        var readBytes = 0
        while (readBytes != -1 && readBytes < REQUEST_SIZE) {
            readBytes += inputStream.read(receivedByteArray, readBytes, REQUEST_SIZE - readBytes)
        }
        return receivedByteArray
    }
}
