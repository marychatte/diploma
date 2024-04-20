package netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import utils.RESPONSE

class NettyChannelInitializer : ChannelInitializer<SocketChannel>() {
    lateinit var serverChannel: Channel

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {

            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                msg as ByteBuf
                ctx.writeAndFlush(Unpooled.copiedBuffer(RESPONSE))
            }

            override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { }
        })
    }
}
