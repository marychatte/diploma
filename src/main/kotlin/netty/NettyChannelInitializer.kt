package netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import utils.DATA_ARRAY
import utils.DATA_ARRAY_SIZE

class NettyChannelInitializer(
    val numberOfClients: Int,
) : ChannelInitializer<SocketChannel>() {
    lateinit var serverChannel: Channel
    var clientAccepted = 0
    var timeNano: Long = -1
        private set

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
            override fun channelActive(ctx: ChannelHandlerContext) {
                if (timeNano == -1L) {
                    timeNano = System.nanoTime()
                }
                clientAccepted++

                ctx.write(Unpooled.copiedBuffer(DATA_ARRAY))
                ctx.flush()
            }

            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                msg as ByteBuf
                assert(msg.readableBytes() == DATA_ARRAY_SIZE) {
                    "Server read ${msg.readableBytes()} but should $DATA_ARRAY_SIZE"
                }
            }

            override fun channelInactive(ctx: ChannelHandlerContext) {
                if (clientAccepted == numberOfClients) {
                    timeNano = System.nanoTime() - timeNano
                    serverChannel.close()
                }
            }
        })
    }
}
