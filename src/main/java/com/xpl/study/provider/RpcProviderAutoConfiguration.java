package com.xpl.study.provider;

import com.xpl.study.commons.RpcProperties;
import com.xpl.study.registry.ServiceRegistry;
import com.xpl.study.registry.ServiceRegistryFactory;
import com.xpl.study.registry.ServiceRegistryType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * starter封装
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfiguration {

    @Resource
    private RpcProperties rpcProperties;

    @Bean
    public RpcProvider init() throws Exception {
        ServiceRegistryType type = ServiceRegistryType.valueOf(rpcProperties.getServiceRegistryType());
        ServiceRegistry serviceRegistry = ServiceRegistryFactory.getInstance(type, rpcProperties.getServiceRegistryAddress());
        return new RpcProvider(rpcProperties.getServiceAddress(), serviceRegistry);
    }
}
