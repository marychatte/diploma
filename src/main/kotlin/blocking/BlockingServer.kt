package blocking

import utils.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.SocketException
import java.net.StandardSocketOptions

class BlockingServer {
    private val serverSocket = ServerSocket(SERVER_PORT, SERVER_BACKLOG).apply {
        soTimeout = Int.MAX_VALUE
        setOption(StandardSocketOptions.SO_REUSEPORT, true)
        setOption(StandardSocketOptions.SO_REUSEADDR, true)
    }

    fun start() {
        while (true) {
            handleClient()
        }
    }

    fun stop() {
        serverSocket.close()
    }

    private fun handleClient() {
        val clientSocket = serverSocket.accept()
        Thread {
            try {
                while (true) {
                    val inputStream = DataInputStream(clientSocket.getInputStream())
                    val receivedByteArray = read(inputStream)

                    if (!DEBUG) {
                        if (!receivedByteArray.isHttpRequest()) {
                            break
                        }
                    }

                    if (DEBUG) {
                        receivedByteArray.checkRequest()
                    }

                    val outputStream = DataOutputStream(clientSocket.getOutputStream())
                    outputStream.write(RESPONSE)
                    outputStream.flush()

                    if (DEBUG) {
                        break
                    }
                }
            } catch (_: SocketException) {
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            } finally {
                clientSocket.close()
            }
        }.start()
    }

    private fun read(inputStream: DataInputStream): ByteArray {
        val receivedByteArray = ByteArray(REQUEST_SIZE)
        var readBytes = 0
        while (readBytes != -1 && readBytes < REQUEST_SIZE) {
            readBytes += inputStream.read(receivedByteArray, readBytes, REQUEST_SIZE - readBytes)
        }
        return receivedByteArray
    }
}
