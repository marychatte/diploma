package blocking

import utils.REQUEST
import utils.RESPONSE
import utils.RESPONSE_SIZE
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class BlockingClient(private val serverAddress: String, private val serverPort: Int) {
    fun start() {
        val clientSocket = Socket(serverAddress, serverPort)

        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.write(REQUEST)
        outputStream.flush()

        val inputStream = DataInputStream(clientSocket.getInputStream())
        val receivedByteArray = ByteArray(RESPONSE_SIZE)
        inputStream.read(receivedByteArray)

        require(receivedByteArray.contentEquals(RESPONSE))

        clientSocket.close()
    }
}
