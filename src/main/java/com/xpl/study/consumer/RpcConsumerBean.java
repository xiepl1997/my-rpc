package com.xpl.study.consumer;

import com.xpl.study.registry.ServiceRegistryFactory;
import com.xpl.study.registry.ServiceRegistryType;
import org.springframework.beans.factory.FactoryBean;

/**
 * Rpc消费者bean
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class RpcConsumerBean implements FactoryBean {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddress;

    private Object object;

    @Override
    public Object getObject() throws Exception {
        return this.object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void init() throws Exception {
        this.object = RpcConsumer.create(interfaceClass, serviceVersion, ServiceRegistryFactory.getInstance(
                ServiceRegistryType.valueOf(registryType), registryAddress
        ));
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }
}
