package blocking

import utils.DATA_ARRAY
import utils.DATA_ARRAY_SIZE
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import kotlin.system.measureNanoTime

class BlockingServer(
    private val numberOfClients: Int = 1,
    private val serverPort: Int,
) {
    private val serverSocket = ServerSocket(serverPort, numberOfClients + 1)
    var timeNano: Long = 0
        private set

    fun start() {
        timeNano = measureNanoTime {
            repeat(numberOfClients) {
                handleClient()
            }
        }
    }

    fun stop() {
        serverSocket.close()
    }

    private fun handleClient() {
        val clientSocket = serverSocket.accept()

        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.write(DATA_ARRAY)
        outputStream.flush()

        val inputStream = DataInputStream(clientSocket.getInputStream())
        val receivedByteArray = ByteArray(DATA_ARRAY_SIZE)
        inputStream.read(receivedByteArray)

        clientSocket.close()
    }
}
