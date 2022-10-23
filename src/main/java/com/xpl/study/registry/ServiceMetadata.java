package com.xpl.study.registry;

import lombok.Data;

/**
 * 服务元数据
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Data
public class ServiceMetadata {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 服务版本
     */
    private String serviceVersion;

    /**
     * 服务提供方地址
     */
    private String address;

    /**
     * 服务暴露端口
     */
    private int port;

    public static ServiceMetadata builder() {
        return new ServiceMetadata();
    }

    public ServiceMetadata serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceMetadata serviceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    public ServiceMetadata address(String address) {
        this.address = address;
        return this;
    }

    public ServiceMetadata port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceMetadata{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", serviceVersion='").append(serviceVersion).append('\'');
        sb.append(", address='").append(address).append('\'');
        sb.append(", port='").append(port);
        sb.append('}');
        return sb.toString();
    }
}
