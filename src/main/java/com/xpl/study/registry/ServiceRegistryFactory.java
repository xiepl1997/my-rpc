package com.xpl.study.registry;

/**
 * 服务注册工厂类
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class ServiceRegistryFactory {

    private static volatile ServiceRegistry serviceRegistry;

    public static ServiceRegistry getInstance(ServiceRegistryType type, String registryAddress) throws Exception {
        if (null == serviceRegistry) {
            synchronized (ServiceRegistryFactory.class) {
                if (null == serviceRegistry) {
                    serviceRegistry = type == ServiceRegistryType.zookeeper ? new ZookeeperServiceRegistry(registryAddress) :
                        type == ServiceRegistryType.eureka ? new EurekaServiceRegistry(registryAddress) : null;
                }
            }
        }
        return serviceRegistry;
    }
}
