package netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel


class NettyServer(private val numberOfClients: Int, private val serverPort: Int) {
    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()

    private val nettyChannelInitializer = NettyChannelInitializer(numberOfClients)

    var timeNano: Long = -1
        get() = nettyChannelInitializer.timeNano
        private set

    fun start() {
        val serverBootstrap = ServerBootstrap()
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, numberOfClients)
            .childHandler(nettyChannelInitializer)

        val future = serverBootstrap.bind(serverPort).sync()
        nettyChannelInitializer.serverChannel = future.channel()
        future!!.channel().closeFuture().sync()
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}