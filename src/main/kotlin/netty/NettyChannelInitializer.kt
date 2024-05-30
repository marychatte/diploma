package netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import utils.RESPONSE

class NettyChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(object : SimpleChannelInboundHandler<ByteBuf>() {
            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {

                ctx.write(Unpooled.copiedBuffer(RESPONSE))
            }

            override fun channelReadComplete(ctx: ChannelHandlerContext) {
                ctx.flush()
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
                ctx?.close()
            }
        })
    }
}
