package org.asciidoctor.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class TestHttpServer {


    private static final int MAX_MESSAGE_LENGTH = 512 * 1024;

    private static TestHttpServer instance;

    Channel serverChannel;
    EventLoopGroup eventLoopGroup;
    Map<String, File> resources;

    private TestHttpServer(Map<String, File> resources) {
        this.resources = resources;

        eventLoopGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("codec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_MESSAGE_LENGTH));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                final FullHttpRequest request = (FullHttpRequest) msg;
                                final File resourceFile = TestHttpServer.instance.resources.get(request.getUri());
                                final HttpVersion protocolVersion = request.getProtocolVersion();

                                if (resourceFile == null) {
                                    ByteBuf notFoundResponse = Unpooled.copiedBuffer("<html><head/><body><h1>Sorry, no content found</h1></body>".getBytes());
                                    ctx.writeAndFlush(new DefaultFullHttpResponse(protocolVersion, NOT_FOUND, notFoundResponse));
                                } else {
                                    ByteBuf content = Unpooled.copiedBuffer(Files.readAllBytes(resourceFile.toPath()));
                                    ctx.writeAndFlush(new DefaultFullHttpResponse(protocolVersion, OK, content));
                                }
                                ctx.close();
                            }
                        });
                    }
                });
        ChannelFuture future = bootstrap.bind(0);
        future.syncUninterruptibly();
        this.serverChannel = future.channel();
    }

    public static void start(Map<String, File> resources) {

        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new TestHttpServer(resources);
    }

    public static TestHttpServer getInstance() {
        return instance;
    }

    public int getLocalPort ( ) {
        return ((InetSocketAddress) serverChannel.localAddress()).getPort();
    }

    public void stop() {
        this.serverChannel.close();
        this.eventLoopGroup.shutdownGracefully();
        instance = null;
    }

}
