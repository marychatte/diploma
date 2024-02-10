package nonblocking

import utils.DATA_ARRAY_SIZE
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

        if (writeCount >= DATA_ARRAY_SIZE) {
            break
        }
    }
    selectorManager.deleteInterest(selectionKey, SelectionKey.OP_WRITE)
}

suspend fun SocketChannel.readFrom(selectionKey: SelectionKey, selectorManager: ReactorSelectorManager): ByteBuffer {
    var readCount = 0
    val readBuffer = ByteBuffer.allocate(DATA_ARRAY_SIZE)

    while (true) {
        selectorManager.select(selectionKey, SelectionKey.OP_READ)

        val curReadCount = read(readBuffer)
        readCount += curReadCount

        if (curReadCount == -1 || readCount >= DATA_ARRAY_SIZE) {
            break
        }
    }
    selectorManager.deleteInterest(selectionKey, SelectionKey.OP_READ)
    return readBuffer
}