package com.xpl.study.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * Rpc请求类
 *
 * @author peiliang xie
 * @date 2022/10/23
 */
@Data
public class RpcRequest implements Serializable {

    private String requestId;

    private String className;

    private String methodName;

    private String serviceVersion;

    private Class<?>[] parameterTypes;

    private Object[] parameters;
}
