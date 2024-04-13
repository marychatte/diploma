package blocking

import utils.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.StandardSocketOptions

class BlockingServer {
    private val serverSocket = ServerSocket(SERVER_PORT, SERVER_BACKLOG).apply {
        soTimeout = Int.MAX_VALUE
        setOption(StandardSocketOptions.SO_REUSEPORT, true)
        setOption(StandardSocketOptions.SO_REUSEADDR, true)
    }
    var timeNano: Long = -1

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
                    val receivedByteArray = ByteArray(REQUEST_SIZE)
                    inputStream.read(receivedByteArray)

                    if (!receivedByteArray.isHttRequest()) break

//                    receivedByteArray.checkRequest()

                    val outputStream = DataOutputStream(clientSocket.getOutputStream())
                    outputStream.write(RESPONSE)
                    outputStream.flush()
                }
                clientSocket.close()
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        }.start()
    }
}
