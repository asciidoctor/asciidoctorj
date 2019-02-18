package org.asciidoctor.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import org.asciidoctor.jruby.internal.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Map;

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
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("codec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_MESSAGE_LENGTH));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                FullHttpRequest request = (FullHttpRequest) msg;
                                File resourceFile = TestHttpServer.instance.resources.get(request.getUri());

                                if (resourceFile == null) {
                                    ByteBuf notFoundResponse = Unpooled.copiedBuffer("<html><head/><body><h1>Sorry, no content found</h1></body>".getBytes());
                                    ctx.writeAndFlush(new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND, notFoundResponse));
                                } else {
                                    InputStream in = null;
                                    try {
                                        in = new FileInputStream(resourceFile);
                                        String response = IOUtils.readFull(in);
                                        ctx.writeAndFlush(new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK, Unpooled.copiedBuffer(response.getBytes())));
                                    } finally {
                                        in.close();
                                    }
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
