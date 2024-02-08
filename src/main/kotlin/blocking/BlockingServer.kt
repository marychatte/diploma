package blocking

import utils.DATA_ARRAY
import utils.DATA_ARRAY_SIZE
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

class BlockingServer(private val numberOfClients: Int = 1, private val serverPort: Int) {
    private val serverSocket = ServerSocket(serverPort, numberOfClients + 1)
    var timeNano: Long = -1
        private set

    fun start() {
        repeat(numberOfClients) {
            handleClient()
        }

        timeNano = System.nanoTime() - timeNano
    }

    fun stop() {
        serverSocket.close()
    }

    private fun handleClient() {
        val clientSocket = serverSocket.accept()
        if (timeNano == -1L) {
            timeNano = System.nanoTime()
        }

        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.write(DATA_ARRAY)
        outputStream.flush()

        val inputStream = DataInputStream(clientSocket.getInputStream())
        val receivedByteArray = ByteArray(DATA_ARRAY_SIZE)
        inputStream.read(receivedByteArray)

        clientSocket.close()
    }
}
