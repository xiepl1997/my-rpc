package com.xpl.study.provider;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xpl.study.annotation.RPCProvider;
import com.xpl.study.commons.ProviderUtils;
import com.xpl.study.protocol.RpcDecoder;
import com.xpl.study.protocol.RpcEncoder;
import com.xpl.study.registry.ServiceMetadata;
import com.xpl.study.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xpl.study.constans.Constants.PROVIDER_THREAD_POOL_NUM;
import static com.xpl.study.constans.Constants.PROVIDER_THREAD_POOL_QUEUE_LEN;

/**
 * 服务提供者
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-provider-pool-%d").build();

    private static ThreadPoolExecutor threadPoolExecutor;

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>(256);

    private EventLoopGroup bossGroup = null;

    private EventLoopGroup workerGroup = null;

    public RpcProvider(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProvider(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Runnable task) {
        if (null == threadPoolExecutor) {
            synchronized (RpcProvider.class) {
                if (null == threadPoolExecutor) {
                    threadPoolExecutor = new ThreadPoolExecutor(PROVIDER_THREAD_POOL_NUM, PROVIDER_THREAD_POOL_QUEUE_LEN,
                        600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(PROVIDER_THREAD_POOL_QUEUE_LEN),
                        threadFactory);
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 手动注册一个服务，主要用于测试
     *
     * @param providerBean  服务提供方的bean
     * @param serverAddress 服务提供方地址
     */
    public void addService(Object providerBean, String serverAddress) {
        RPCProvider rpcProvider = providerBean.getClass().getAnnotation(RPCProvider.class);
        String serviceName = rpcProvider.serviceInterface().getName();
        String version = rpcProvider.serviceVersion();
        String providerKey = ProviderUtils.makeKey(serviceName, version);
        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        ServiceMetadata metadata = ServiceMetadata.builder()
            .address(host)
            .serviceName(serviceName)
            .port(port)
            .serviceVersion(version);

        try {
            serviceRegistry.register(metadata);
            log.debug("register service: {}", metadata.toString());
        } catch (Exception e) {
            log.error("register service fail|{}|{}", metadata.toString(), e);
        }

        if (!handlerMap.containsKey(providerKey)) {
            log.info("Loading service: {}", providerKey);
            handlerMap.put(providerKey, providerBean);
        }
    }

    /**
     * 开启netty服务监听，进行服务注册
     *
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        if (null == bossGroup || null == workerGroup) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 通用平台使用NioServerSocketChannel，Linux使用EpollServerSocketChannel
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0))
                            .addLast(new RpcDecoder())
                            .addLast(new RpcEncoder())
                            .addLast(new RpcProviderHandler(handlerMap));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            // 启动服务监听
            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("Server started on port {}", port);

            // 同步等待，会hang住线程，所以需要单独开一个线程来调用start方法
            future.channel().closeFuture().sync();
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RPCProvider rpcProvider = bean.getClass().getAnnotation(RPCProvider.class);
        if (null == rpcProvider) {
            return bean;
        }
        String serviceName = rpcProvider.serviceInterface().getName();
        String version = rpcProvider.serviceVersion();
        String providerKey = ProviderUtils.makeKey(serviceName, version);

        // 缓存provider bean到本地缓存中
        handlerMap.put(providerKey, bean);

        // 注册服务到注册中心
        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[0]);

        ServiceMetadata metadata = ServiceMetadata.builder()
            .serviceName(serviceName)
            .serviceVersion(version)
            .address(host)
            .port(port);

        try {
            serviceRegistry.register(metadata);
            log.debug("register service: {}", metadata.toString());
        } catch (Exception e) {
            log.error("register service fail|{}|{}", metadata.toString(), e);
        }
        return bean;
    }
}
