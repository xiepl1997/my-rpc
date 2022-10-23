package com.xpl.study.registry;

/**
 * 服务治理
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     *
     * @param serviceMetadata 服务元数据
     * @throws Exception      异常
     */
    void register(ServiceMetadata serviceMetadata) throws Exception;

    /**
     * 服务注销
     *
     * @param serviceMetadata 服务元数据
     * @throws Exception      异常
     */
    void unRegister(ServiceMetadata serviceMetadata) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceName 服务名
     * @return            返回服务发现的结果
     * @throws Exception  异常
     */
    ServiceMetadata discovery(String serviceName) throws Exception;

    /**
     * 关闭
     *
     * @throws Exception 异常
     */
    void close() throws Exception;
}
