import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class Client(
    private val serverAddress: String,
    private val serverPort: Int,
) {
    fun start() {
        val clientSocket = Socket(serverAddress, serverPort)

        val inputStream = DataInputStream(clientSocket.getInputStream())
        val receivedByteArray = ByteArray(DATA_ARRAY_SIZE)
        inputStream.read(receivedByteArray)

        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.write(receivedByteArray)
        outputStream.flush()
        clientSocket.close()
    }
}
