package reactor

import utils.REQUEST_SIZE
import utils.RESPONSE_SIZE
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

suspend fun SocketChannel.writeTo(
    selectionKey: SelectionKey,
    selectorManager: ReactorSelectorManager,
    buffer: ByteBuffer
) {
    var writeCount = 0
    while (true) {
        selectorManager.select(selectionKey, SelectionKey.OP_WRITE)
        val curWriteCount = write(buffer)
        writeCount += curWriteCount

        if (writeCount == RESPONSE_SIZE) {
            break
        }
    }
}

suspend fun SocketChannel.readFrom(selectionKey: SelectionKey, selectorManager: ReactorSelectorManager): ByteBuffer {
    var readCount = 0
    val readBuffer = ByteBuffer.allocate(REQUEST_SIZE)

    while (true) {
        selectorManager.select(selectionKey, SelectionKey.OP_READ)

        val curReadCount = read(readBuffer)
        readCount += curReadCount

        if (curReadCount == -1 || readCount >= REQUEST_SIZE) {
            break
        }
    }
    return readBuffer
}