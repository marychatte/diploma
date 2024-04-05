package netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import utils.REQUEST_SIZE
import utils.RESPONSE

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

                ctx.write(Unpooled.copiedBuffer(RESPONSE))
                ctx.flush()
            }

            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                msg as ByteBuf
                assert(msg.readableBytes() == REQUEST_SIZE) {
                    "Server read ${msg.readableBytes()} but should $REQUEST_SIZE"
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
