package com.xpl.study.commons;

/**
 * 服务提供工具类
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
public class ProviderUtils {

    /**
     * 构造服务的唯一标识key
     *
     * @param serviceName    服务名称
     * @param serviceVersion 服务版本
     * @return 服务标识
     */
    public static String makeKey(String serviceName, String serviceVersion) {
        return String.join(":", serviceName, serviceVersion);
    }
}
