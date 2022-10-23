package com.xpl.study.consumer;

import com.xpl.study.commons.ProviderUtils;
import com.xpl.study.protocol.RpcDecoder;
import com.xpl.study.protocol.RpcEncoder;
import com.xpl.study.protocol.RpcRequest;
import com.xpl.study.protocol.RpcResponse;
import com.xpl.study.registry.ServiceMetadata;
import com.xpl.study.registry.ServiceRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * 服务消费者
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Slf4j
public class RpcConsumer extends SimpleChannelInboundHandler<RpcResponse> {

    private final Object obj = new Object();

    private ServiceRegistry serviceRegistry;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    private Channel channel;

    private RpcResponse rpcResponse;

    public RpcConsumer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public static <T> T create(Class<T> interfaceClass, String serviceVersion, ServiceRegistry serviceRegistry) {
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[] {interfaceClass},
            new RpcInvokeHandler<>(serviceVersion, serviceRegistry)
        );
    }

    public RpcResponse sendRequest(RpcRequest rpcRequest) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                            .addLast(new RpcEncoder())
                            .addLast(new RpcDecoder())
                            .addLast(RpcConsumer.this);
                    }
                });
            String targetService = ProviderUtils.makeKey(rpcRequest.getClassName(), rpcRequest.getServiceVersion());
            // 寻找服务
            ServiceMetadata serviceMetadata = serviceRegistry.discovery(targetService);
            if (null == serviceMetadata) {
                // 没有获取到服务提供方
                throw new RuntimeException("no available service provider for " + targetService);
            }
            final ChannelFuture future = bootstrap.connect(serviceMetadata.getAddress(), serviceMetadata.getPort()).sync();

            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.debug("connect rpc provider success");
                } else {
                    log.error("connect rpc provider fail");
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully(); // 关闭线程组
                }
            });

            this.channel = future.channel();
            // 发送请求
            this.channel.writeAndFlush(rpcRequest).sync();

            synchronized (this.obj) {
                this.obj.wait();
            }

            return this.rpcResponse;
        } finally {
            close();
        }
    }

    /**
     * 客户端关闭
     */
    private void close() {
        // 关闭客户端套接字
        if (this.channel != null) {
            this.channel.close();
        }
        // 关闭客户端线程组
        if (this.eventLoopGroup != null) {
            this.eventLoopGroup.shutdownGracefully();
        }
        log.debug("shutdown consumer...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.rpcResponse = rpcResponse;
        synchronized (this.obj) {
            // 收到响应，唤醒线程
            obj.notifyAll();
        }
    }
}
