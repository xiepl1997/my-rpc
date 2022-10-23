package com.xpl.study.commons;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rpc配置
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    private String serviceAddress;

    private String serviceRegistryAddress;

    private String serviceRegistryType;
}
