package com.xpl.study.registry;

/**
 * todo 使用eureka实现服务治理
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class EurekaServiceRegistry implements ServiceRegistry{

    public EurekaServiceRegistry(String address) {
    }

    @Override
    public void register(ServiceMetadata serviceMetadata) throws Exception {

    }

    @Override
    public void unRegister(ServiceMetadata serviceMetadata) throws Exception {

    }

    @Override
    public ServiceMetadata discovery(String serviceName) throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
