package blocking

import utils.REQUEST_SIZE
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class BlockingClient(private val serverAddress: String, private val serverPort: Int) {
    fun start() {
        val clientSocket = Socket(serverAddress, serverPort)

        val inputStream = DataInputStream(clientSocket.getInputStream())
        val receivedByteArray = ByteArray(REQUEST_SIZE)
        inputStream.read(receivedByteArray)

        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.write(receivedByteArray)
        outputStream.flush()
        clientSocket.close()
    }
}
