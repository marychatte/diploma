package netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import run.SELECTOR_THREADS_COUNT
import run.ports
import utils.SERVER_BACKLOG

class NettyServer {
    private val eventloopGroup = NioEventLoopGroup(SELECTOR_THREADS_COUNT)

    private val nettyChannelInitializer = NettyChannelInitializer()

    fun start() {
        val serverBootstrap = ServerBootstrap()
        serverBootstrap.group(eventloopGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, SERVER_BACKLOG)
            .childHandler(nettyChannelInitializer)

        ports().map { serverPort ->
            serverBootstrap.bind(serverPort).sync()
        }.forEach { future ->
            future!!.channel().closeFuture().sync()
        }
    }

    fun stop() {
        eventloopGroup.shutdownGracefully()
    }
}