package eventloop

import utils.DATA_ARRAY_SIZE
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

suspend fun SocketChannel.writeTo(
    eventLoopManager: EventLoopManager,
    buffer: ByteBuffer
) {
    var writeCount = 0
    while (true) {
        val curWriteCount = eventLoopManager.addEvent {
            write(buffer)
        } as Int
        writeCount += curWriteCount

        if (writeCount >= DATA_ARRAY_SIZE) {
            break
        }
    }
}

suspend fun SocketChannel.readFrom(
    eventLoopManager: EventLoopManager,
): ByteBuffer {
    var readCount = 0
    val readBuffer = ByteBuffer.allocate(DATA_ARRAY_SIZE)

    while (true) {
        val curReadCount = eventLoopManager.addEvent {
            read(readBuffer)
        } as Int
        readCount += curReadCount

        if (curReadCount == -1 || readCount >= DATA_ARRAY_SIZE) {
            break
        }
    }
    return readBuffer
}