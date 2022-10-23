package com.xpl.study.registry;

import com.xpl.study.commons.ProviderUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xpl.study.constans.Constants.BASE_PATH;

/**
 * zookeeper作为服务治理实现
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class ZookeeperServiceRegistry implements ServiceRegistry{

    private final CuratorFramework client;

    private final Object lock = new Object();

    private ServiceDiscovery<ServiceMetadata> serviceDiscovery;

    /**
     * 本地缓存，避免不必要的网络请求
     */
    private Map<String, ServiceProvider<ServiceMetadata>> serviceProviderCache;

    private List<Closeable> closeableProviders = Lists.newArrayList();

    public ZookeeperServiceRegistry(String address) throws Exception {
        serviceProviderCache = new ConcurrentHashMap<>(256);
        this.client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
        this.client.start();
        JsonInstanceSerializer<ServiceMetadata> serializer = new JsonInstanceSerializer<>(ServiceMetadata.class);
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetadata.class)
            .client(this.client)
            .serializer(serializer)
            .basePath(BASE_PATH)
            .build();
        serviceDiscovery.start();
    }

    @Override
    public void register(ServiceMetadata serviceMetadata) throws Exception {
        ServiceInstance<ServiceMetadata> serviceInstance = ServiceInstance
            .<ServiceMetadata>builder()
            // 使用{服务名}:{服务版本}唯一标识一个服务
            .name(ProviderUtils.makeKey(serviceMetadata.getServiceName(), serviceMetadata.getServiceVersion()))
            .address(serviceMetadata.getAddress())
            .port(serviceMetadata.getPort())
            .payload(serviceMetadata)
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMetadata serviceMetadata) throws Exception {
        ServiceInstance<ServiceMetadata> serviceInstance = ServiceInstance
                .<ServiceMetadata>builder()
                // 使用{服务名}:{服务版本}唯一标识一个服务
                .name(ProviderUtils.makeKey(serviceMetadata.getServiceName(), serviceMetadata.getServiceVersion()))
                .address(serviceMetadata.getAddress())
                .port(serviceMetadata.getPort())
                .payload(serviceMetadata)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceMetadata discovery(String serviceName) throws Exception {
        // 先读缓存
        ServiceProvider<ServiceMetadata> serviceProvider = serviceProviderCache.get(serviceName);
        if (null == serviceProvider) {
            synchronized(lock) {
                serviceProvider = serviceDiscovery
                    .serviceProviderBuilder()
                    .serviceName(serviceName)
                    // 设置负载均衡策略，这里使用轮询
                    .providerStrategy(new RoundRobinStrategy<>())
                    .build();
                serviceProvider.start();
                closeableProviders.add(serviceProvider);
                serviceProviderCache.put(serviceName, serviceProvider);
            }
        }
        ServiceInstance<ServiceMetadata> serviceInstance = serviceProvider.getInstance();
        return serviceInstance != null ? serviceInstance.getPayload() : null;
    }

    @Override
    public void close() throws Exception {
        for (Closeable closeable : closeableProviders) {
            CloseableUtils.closeQuietly(closeable);
        }
        serviceDiscovery.close();
    }
}
