import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import kotlin.properties.Delegates
import kotlin.system.measureNanoTime

class Server(
    private val numberOfClients: Int = 1,
    serverPort: Int,
) {
    var timeNano: Long by Delegates.notNull()
        private set

    private val serverSocket = ServerSocket(serverPort, numberOfClients + 1)

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
